package mq_002;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsProduceTopic {
    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "topic01";

    public static void main(String[] args) throws JMSException {
        //1:创建连接工厂，才用默认的用户名和密码
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        //2：通过连接工程，获取连接connection并启动访问
        final Connection connection = factory.createConnection();
        connection.start();
        //3:创建会话session（两个参数：第一个是事物；第二个是签收）
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //4:创建目的地（具体是队列或者主题）
//        final Destination destination = session.createTopic(QUEUE_NAME); //和下面的一个意思
        final Topic topic = session.createTopic(QUEUE_NAME);
        //5:创建生产者
        final MessageProducer producer = session.createProducer(topic);
        //6:通过生产者生产3条消息发送到MQ队列中
        for (int i = 0; i < 4; i++) {
            //7:创建消息
            final TextMessage message = session.createTextMessage("msg----" + i);
            //8:通过生产者上传
            producer.send(message);
        }
        //9:释放资源
        producer.close();
        session.close();
        connection.close();

        System.out.println("*************消息发布成功***********");
    }
}
