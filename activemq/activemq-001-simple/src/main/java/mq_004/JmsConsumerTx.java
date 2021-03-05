package mq_004;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Objects;

public class JmsConsumerTx {

    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "queue01_tx";

    public static void main(String[] args) throws JMSException {
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        final Connection connection = factory.createConnection();
        connection.start();
        final Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
        final Destination destination = session.createQueue(QUEUE_NAME);
        final MessageConsumer consumer = session.createConsumer(destination);
        while (true){
            final TextMessage textMessage = (TextMessage) consumer.receive(3000);
            if (Objects.isNull(textMessage)){
                break;
            }
            textMessage.acknowledge();
            System.out.println("****接收到的消息****"+textMessage.getText());
        }
        //这里如果没有commit，那么就会被MQ判定为未消费
        session.commit();
        consumer.close();
        session.close();
        connection.close();
        System.out.println("*********consumer is end******");
    }
}
