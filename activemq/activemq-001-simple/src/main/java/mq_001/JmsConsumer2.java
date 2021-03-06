package mq_001;

import lombok.SneakyThrows;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.util.Objects;

public class JmsConsumer2 {

    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "queue01";

    public static void main(String[] args) throws JMSException, IOException {
        //1:创建连接工厂，才用默认的用户名和密码
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        //2：通过连接工程，获取连接connection并启动访问
        final Connection connection = factory.createConnection();
        connection.start();
        //3:创建会话session（两个参数：第一个是事物；第二个是签收）
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //4:创建目的地（具体是队列或者主题）
        final Destination destination = session.createQueue(QUEUE_NAME);
        //5:创建消费者
        final MessageConsumer consumer = session.createConsumer(destination);
        //6:通过监听方式接收消息
        consumer.setMessageListener(new MessageListener() {
            @SneakyThrows
            @Override
            public void onMessage(Message message) {
                if (Objects.nonNull(message)){
                    if (message instanceof TextMessage){
                        final TextMessage textMessage = (TextMessage) message;
                        System.out.println("*******接收到的消息2*******"+textMessage.getText());
                    }
                }
            }
        });
        System.out.println("*********consumer is end******");
        //保证控制台不灭，摁下任意键关闭,防止程序跑太快，监听器还没接收到消息就关闭了
        System.in.read();
        consumer.close();
        session.close();
        connection.close();
    }
}
