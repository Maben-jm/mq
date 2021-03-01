[TOC]

#  MQ学习

> 作用：MQ中最大的作用就是将系统进行解耦操作，减缓主系统的接口压力；
>
> 特点：解耦 | 削峰 | 异步

## 1.MQ目前流行的几种框架

> 目前比较流行的MQ框架主要有：KAFKA,ACTIVE MQ,RABBITMQ,ROCKETMQ等；
>
> kafka：           编程语言Java
>
> activeMq：    编程语言Java              符合JMS规范
>
> rabbitMq:      编程语言erlang
>
> rocketMq:      编程语言Java

##  2.activeMQ学习

> 官网：http://activemq.apache.org
>
> 安装包：直接在官网下载最新的：这里使用的是ActiveMQ 5.16.1 (Jan 20, 2021)
>
> 默认端口：61616
>
> 两大模式：
>
> ​	point-to-point（1-1）: sender    --------------->   queue    
>
> ​    Publish-and-Subscribe（1-Many）:   publisher  --------------> topic 

### 2.1 命令相关

#### 2.1.1 启动命令

````
> 启动：
	 bin/activemq start
> 重新启动
	bin/activemq restart
> 带日志的启动
  bin/activemq start > ./log/myActiveMq.log
````

#### 2.1.2查看启动

````
jps 
ps -ef|grep activemq
netstat -anp|grep 61616
lsof -i:61616
````

#### 2.1.3 关闭

````
bin/activemq stop
````

### 2.2 activeMq控制台

````
默认访问连接：http://localhost:8161/admin/
默认用户和密码： admin/admin
````

### 2.3 开发简单activeMq项目

#### 2.3.1 xml文件

````xml
				<!--activemq包-->
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-all</artifactId>
            <version>5.15.9</version>
        </dependency>
        <dependency>
            <groupId>org.apache.xbean</groupId>
            <artifactId>xbean-spring</artifactId>
            <version>3.16</version>
        </dependency>
````

#### 2.3.2  queue格式

##### 2.3.2.1 生产者（queue）

````java
package mq_001;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsProduce {
    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "queue01";
    public static void main(String[] args) throws JMSException {
        //1:创建连接工厂，才用默认的用户名和密码
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        //2：通过连接工程，获取连接connection并启动访问
        final Connection connection = factory.createConnection();
        connection.start();
        //3:创建会话session（两个参数：第一个是事物；第二个是签收）
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //4:创建目的地（具体是队列或者主题）
        final Destination destination = session.createQueue(QUEUE_NAME);
        //5:创建消息的生产者
        final MessageProducer producer = session.createProducer(destination);
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
````

##### 2.3.2.2 消费者1（queue）

````java
package mq_001;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Objects;

public class JmsConsumer {

    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "queue01";

    public static void main(String[] args) throws JMSException {
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
        while (true){
            /**
             * receive()方法：是说线程阻塞的，一直在这等着接收消息，所以textMessage不会为空
             * receive(long timeout)方法:是线程非阻塞的，一旦没有消息了，就会返回空
             */
            final TextMessage textMessage = (TextMessage) consumer.receive(3000);
            if (Objects.isNull(textMessage)){
                break;
            }
            System.out.println("****接收到的消息****"+textMessage.getText());
        }
        consumer.close();
        session.close();
        connection.close();
        System.out.println("*********consumer is end******");
    }
}

````

##### 2.3.2.3 消费者2（queue）

````java
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
````

##### 2.3.2.4 总结

````
情景一：一个生产者生产了6调数据，先启动一个消费者，此消费者消费6条后再启动另外一个消费者，那么之后启动的消费者将不会再接收到消息；
情景二：有两个消费者，一个生产者，生产者生产6条信息，两个消费端各消费三个；
````

#### 2.3.3 topic格式

##### 2.3.3.1 消费者（topic）

````java
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

````

##### 2.3.3.2 消费者1 （topic）

````java
package mq_002;

import lombok.SneakyThrows;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.util.Objects;

public class JmsConsumerTopic {
    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "topic01";

    public static void main(String[] args) throws JMSException, IOException {

        System.out.println("***********我是一号消费者**********");
//        System.out.println("***********我是二号消费者**********");

        //1:创建连接工厂，才用默认的用户名和密码
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        //2：通过连接工程，获取连接connection并启动访问
        final Connection connection = factory.createConnection();
        connection.start();
        //3:创建会话session（两个参数：第一个是事物；第二个是签收）
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //4:创建目的地（具体是队列或者主题）
        final Destination destination = session.createTopic(QUEUE_NAME);
        //5:创建消费者
        final MessageConsumer consumer = session.createConsumer(destination);
        //6:通过监听方式接收消息
        consumer.setMessageListener(message -> {
            if (Objects.nonNull(message)){
                if (message instanceof TextMessage){
                    final TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("*******接收到的消息*******"+textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
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

````

##### 2.3.3.3 消费者2（topic）

````java
package mq_002;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.util.Objects;

public class JmsConsumerTopic2 {
    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "topic01";

    public static void main(String[] args) throws JMSException, IOException {

//        System.out.println("***********我是一号消费者**********");
        System.out.println("***********我是二号消费者**********");

        //1:创建连接工厂，才用默认的用户名和密码
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        //2：通过连接工程，获取连接connection并启动访问
        final Connection connection = factory.createConnection();
        connection.start();
        //3:创建会话session（两个参数：第一个是事物；第二个是签收）
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //4:创建目的地（具体是队列或者主题）
        final Destination destination = session.createTopic(QUEUE_NAME);
        //5:创建消费者
        final MessageConsumer consumer = session.createConsumer(destination);
        //6:通过监听方式接收消息
        consumer.setMessageListener(message -> {
            if (Objects.nonNull(message)){
                if (message instanceof TextMessage){
                    final TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("*******接收到的消息2*******"+textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
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

````

##### 2.3.3.4 总结

````
情景一：先启动一个消费者，再让一个生产者生产了6调数据，此消费者消费6条后再启动另外一个消费者，那么之后启动的消费者将不会再接收到消息；
情景二：启动两个消费者，再让生产者生产6条消息，那么这两个消费者将会同时收到这6条消息；
****************************************
注：针对publisher and subscribe模式，一定要先启动消费者；加入刚开始没有消费者，生产者直接启动，那么此时生产的消息就变成了废消息，即使之后启动的消费者也不会再接收这些之前的消息了！！！！
````

