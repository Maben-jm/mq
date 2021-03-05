package mq_004;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsProduceTx {
    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "queue01_tx";
    public static void main(String[] args) throws JMSException {
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        final Connection connection = factory.createConnection();
        connection.start();
        final Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(QUEUE_NAME);
        final MessageProducer producer = session.createProducer(destination);
        try {
            //在这里处理业务逻辑
            for (int i = 0; i < 4; i++) {
                final TextMessage message = session.createTextMessage("msg----" + i);
                producer.send(message);
            }
            session.commit();
        }catch (Exception e){
            //报错直接回滚
            session.rollback();
        }
        producer.close();
        session.close();
        connection.close();
        System.out.println("*************消息发布成功***********");

    }
}
