package mq_003;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.util.Objects;

public class JmsConsumerTopicPersist {
    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "topic_persist";

    public static void main(String[] args) throws JMSException, IOException {

        System.out.println("***********我是持久化消费者**********");

        //1:创建连接工厂，才用默认的用户名和密码
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        //2：通过连接工程，获取连接connection并订阅
        final Connection connection = factory.createConnection();
        connection.setClientID("z3");
        //3:创建会话session（两个参数：第一个是事物；第二个是签收）
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //4:创建目的地（具体是队列或者主题）
        final Topic topic = session.createTopic(QUEUE_NAME);
        final TopicSubscriber durableSubscriber = session.createDurableSubscriber(topic, "remark...");
        connection.start();
        Message message = durableSubscriber.receive();
        while (Objects.nonNull(message)) {
            final TextMessage textMessage = (TextMessage) message;
            System.out.println("*******接收到的消息2*******" + textMessage.getText());
            message = durableSubscriber.receive();
        }
        session.close();
        connection.close();

    }
}
