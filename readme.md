[TOC]

#  MQ学习

> 作用：MQ中最大的作用就是将系统进行解耦操作，减缓主系统的接口压力；
>
> 特点：解耦 | 削峰 | 异步

## 1.MQ目前流行的几种框架

| 语言     | 编程语言 | JMS规范     | 吞吐量 | 事物 |
| -------- | -------- | :---------- | ------ | ---- |
| kafka    | Java     |             | 十万级 |      |
| activeMq | Java     | 符合JMS规范 | 万级   | 支持 |
| rabbitMq | erlang   | 符合JMS规范 | 万级   |      |
| rockeMq  | Java     |             | 十万级 |      |

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

### 2.0 JMS

> JMS包含四大属性（一下以activeMq为例）；

* JMS provider：实现JMS接口和规范的消息中间件，也就是我们的MQ服务器；
* JMS produce： 消息生产者，创建和发送JMS消息的客户端应用；
* JMS consumer：消息消费者，接收和处理JMS消息的客户端应用；
* JMS message
  * 消息头
    * JMSDestination    （JMS目标地）要么是队列 || 要么是主题
    * JMSDeliveryMode  （JMS交付模式 ）持久和非持久模式 ；非持久就传递一次，一次过后不管成功与否，消息都没有了。
    * JMSExpiration   （JMS过期） 如果设置成0，则说明该消息永不过期；默认就是永不过期。
    * JMSPriority   （JMS优先级） 0-9：0-4是普通，5-9是加急，JMS不是严格按照优先级排序，但是加急的消息一定是比普通的消息要先到达。
    * JMSMessageId  消息id ：消息的幂等性用到
  * 消息体
    * TextMessage 普通字符串消息，包含一个string
    * MapMessage 一个Map类型的消息，key为string，value为Java的基本类型
    * BytesMessage 二进制数组消息
    * StreamMessage   Java数据流消息
    * ObjectMessage  对象消息，包含一个可序列化的Java对象
  * 消息属性
    * 他们是以「属性名」和「属性值」的形式制定的；可以将属性视为消息头的扩展，属性可以指定一些消息头没有涵盖的附加信息，比如可以在消息属性中指定消息选择器；

### 2.1 命令相关

#### 2.1.1 启动命令

````
> 启动：
	 bin/activemq start
> 重新启动
	bin/activemq restart
> 带日志的启动
  bin/activemq start > ./log/myActiveMq.log
> 指定配置文件启动
  bin/activemq start xbean:file:/user/.../activemq.xml
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

#### 2.3.4 activeMq持久性

##### 2.3.4.1 队列参数设置

````java
// 1：非持久化::当服务器宕机重启之后，消息不存在
producer.setDeliveryMode(DeliveryMode.NOT_PERSISTENT);
//2:持久化：当服务器宕机重启后，消息任然存在
producer.setDeliveryMode(DeliveryMode.PERSISTENT);
````

##### 2.3.4.2 activeMq 队列默认是持久化的

##### 2.3.4.3 activeMq 订阅参数设置（解决2.3.3.4遗留问题）

###### 2.3.4.3.1 topic持久化发送端

````java
package mq_003;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsProduceTopicPersist {
    public static final String ACTIVE_URL = "tcp://127.0.0.1:61616";
    public static final String QUEUE_NAME = "topic_persist";

    public static void main(String[] args) throws JMSException {
        //1:创建连接工厂，才用默认的用户名和密码
        final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(ACTIVE_URL);
        //2：通过连接工程，获取连接connection并启动访问
        final Connection connection = factory.createConnection();
        //3:创建会话session（两个参数：第一个是事物；第二个是签收）
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //4:创建目的地（具体是队列或者主题）
        final Topic topic = session.createTopic(QUEUE_NAME);
        //5:创建生产者
        final MessageProducer producer = session.createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        //注意一定要在这里启动连接！！！！！
        connection.start();
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

###### 2.3.4.3.2 topic持久化消费端

````java
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
````

###### 2.3.4.3.3 总结

````
操作步骤：
	先启动消费端，让消费者「z3」先订阅上topic「topic_persist」；
	之后关闭消费端；
	起送生产端生产数据；
	最后启动消费端，任然能够接受数据；
````

#### 2.3.5 activeMq事物

##### 2.3.5.1 事物生产端

````java
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
````

##### 2.3.5.2 事物消费端

````java
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
        final Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(QUEUE_NAME);
        final MessageConsumer consumer = session.createConsumer(destination);
        while (true){
            final TextMessage textMessage = (TextMessage) consumer.receive(3000);
            if (Objects.isNull(textMessage)){
                break;
            }
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

````

#### 2.3.6 activeMq 签收

##### 2.3.6.1 签收简介

* 自动签收（默认）  Session.AUTO_ACKNOWLEDGE
* 手动签收
  * Session.CLIENT_ACKNOWLEDGE
  * 客户端调用acknowlege手动签收
* 允许重复消息 Session.DUPS_OK_ACKNOWLEDGE (很少用到)

##### 2.3.6.2 手动签收代码

````java
package mq_004;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Objects;
/**
主要针对消费者
*/
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

````

#### 2.3.7 事物和签收注意

````java
如果「事物」开启了，那么就以「事物」优先，即使「签收」开启了，并且没有写「textMessage.acknowledge();」，也会被MQ定位为已经签收了；也就是说「事物」的优先级大于「签收」
````

#### 2.3.8 activeMq的Broker

````java
Broker 相当于一个activeMq的实例；
也就是说：Broker其实就是实现了用代码的形式启动ActiveMQ，将MQ嵌入到Java代码中，一遍随时启动，再用的时候在启动，这样能节省资源，也能保证可靠性；
````

````java
package mq_005;

import org.apache.activemq.broker.BrokerService;

/**
 * 嵌入到Java中的activeMQ
 */
public class EmbedBroker {
    public static void main(String[] args) throws Exception {

        BrokerService brokerService = new BrokerService();
        brokerService.setUseJmx(true);
        brokerService.addConnector("tcp://localhost:61616");
        brokerService.start();

    }
}
````

### 2.4 activeMQ整合Spring

#### 2.4.1 pom.xml文件

````xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.maben</groupId>
    <artifactId>activemq-002-spring</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>utf-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <!--整合spring start-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jms</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>4.3.23.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>1.6.1</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.9.5</version>
        </dependency>
        <dependency>
            <groupId>cglib</groupId>
            <artifactId>cglib</artifactId>
            <version>3.2.0</version>
        </dependency>

        <!--整合spring end-->

        <!--json依赖-->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.11.3</version>
        </dependency>

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
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-pool</artifactId>
            <version>5.15.9</version>
        </dependency>


        <!--log包-->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.2.3</version>
        </dependency>
        <!--lombok包-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.10</version>
        </dependency>
        <!--Junit包-->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
    </dependencies>

</project>
````

#### 2.4.2 spring配置文件

***主配置文件  applicationContext.xml***

````xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd ">

    <!-- 配置文件导入  -->
    <import resource="classpath:spring/*.xml"/>
</beans>
````

***扫描注解配置 anonation.xml***

````xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-4.0.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx-4.0.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
">
    <!--配置包的自动扫描-->
    <context:component-scan base-package="com.maben.activemqSpring"></context:component-scan>

</beans>
````

***activeMQ配置文件 activeMQ.xml***

````xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd
        http://www.springframework.org/schema/mvc
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/tx/spring-tx.xsd
">

    <!--配置生产者-->
    <bean id="jmsFactory" class="org.apache.activemq.pool.PooledConnectionFactory" destroy-method="stop">
        <property name="connectionFactory">
            <bean class="org.apache.activemq.ActiveMQConnectionFactory">
                <property name="brokerURL" value="tcp://127.0.0.1:61616"></property>
            </bean>
        </property>
        <property name="maxConnections" value="100"></property>
    </bean>


    <!--Destination **  point-to-point -->
    <bean id="destinationQueue" class="org.apache.activemq.command.ActiveMQQueue">
        <!--构造注入，设置自己的队列名称-->
        <constructor-arg index="0" value="my-queue-spring"></constructor-arg>
    </bean>

    <!--destination ** topic   publisher and subscribe-->
    <bean id="destinationTopic" class="org.apache.activemq.command.ActiveMQTopic">
        <!--通过构造注入，设置自己队列的名称-->
        <constructor-arg index="0" value="my-topic-spring"></constructor-arg>
    </bean>


    <!--Spring提供的工具类，他具有发送和接收消息等-->
    <bean id="jmsTemplate" class="org.springframework.jms.core.JmsTemplate">
        <property name="connectionFactory" ref="jmsFactory"></property>
        <property name="defaultDestination" ref="destinationQueue"></property>
        <!--<property name="defaultDestination" ref="destinationTopic"></property>-->
        <!--转换器-->
        <property name="messageConverter">
            <bean class="org.springframework.jms.support.converter.SimpleMessageConverter"></bean>
        </property>
    </bean>

    <!--配置监听器-->
    <bean id="jmsContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">
        <property name="connectionFactory" ref="jmsFactory"></property>
        <property name="destination" ref="destinationQueue"></property>
        <property name="messageListener" ref="myListener"></property>
    </bean>

</beans>
````

#### 2.4.3 生产者

````java
package com.maben.activemqSpring.produce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.TextMessage;

/**
 * 注意这个消费者一次只能生产一条消息
 */
@Service
public class SpringMQProducer {
    @Autowired
    private JmsTemplate jmsTemplate;

    public static void main(String[] args){
        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        SpringMQProducer springMQProducer = (SpringMQProducer) applicationContext.getBean("springMQProducer");
        springMQProducer.jmsTemplate.send(session -> {
            final TextMessage textMessage = session.createTextMessage("spring-activemq整合");
            return textMessage;
        });
        System.out.println("**************Spring-activeMQ生产者发送结束**********");
    }
}
````

#### 2.4.4 消费者

````java
package com.maben.activemqSpring.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * 在没有配置监听器的情况下
 * 注意这个消费者一次只能消费一条数据
 */
@Service
public class SpringMQConsumer {

    @Autowired
    private JmsTemplate jmsTemplate;

    public static void main(String[] args){
        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        final SpringMQConsumer springMQConsumer = (SpringMQConsumer) applicationContext.getBean("springMQConsumer");
        final String msg = (String) springMQConsumer.jmsTemplate.receiveAndConvert();
        System.out.println("********消费者收到消息*******"+msg);
    }

}
````

#### 2.4.5 监听器

````java
package com.maben.activemqSpring.listener;

import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Objects;

@Component
public class MyListener implements MessageListener {
    @Override
    public void onMessage(Message message) {
        if (Objects.nonNull(message)){
            if (message instanceof TextMessage){
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("***监听消费**"+textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
````

### 2.5 activeMQ整合springboot

#### 2.5.1 pom.xml文件

````java
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.maben</groupId>
    <artifactId>activemq-003-springboot</artifactId>
    <version>1.0-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.5.RELEASE</version>
        <relativePath></relativePath>
    </parent>

    <properties>
        <project.build.sourceEncoding>utf-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-activemq</artifactId>
            <version>2.1.5.RELEASE</version>
        </dependency>
    </dependencies>

</project>
````

#### 2.5.2 yml文件

````java
server:
  port: 9001
spring:
  activemq:
    broker-url: tcp://127.0.0.1:61616
    user: admin
    password: admin
  jms:
    pub-sub-domain: false # false:Queue(Default) true：Topic


# 自定义参数
my:
  queueName: boot-activemq-queue
  topicName: boot-activemq-topic
````

#### 2.5.3 主启动类

````java
package com.maben.activemqSpringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ActivemqSpringbootMain {
    public static void main(String[] args)throws Exception{
        SpringApplication.run(ActivemqSpringbootMain.class,args);
        System.out.println("****************启动完成！！！******************");
    }
}
````



#### 2.5.4  Java配置类

````java
package com.maben.activemqSpringboot.config;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import javax.jms.Topic;

/**
 * 配置bean的Java类
 */
@Component
@EnableJms //开启JMS相关注解
public class ConfigBean {

    /**
     * 动态获取队列名称
     */
    @Value("${my.queueName}")
    private String queueName;

    /**
     * 动态获取主题名称
     */
    @Value("${my.topicName}")
    private String topicName;

    /**
     * 注入队列
     * @return Queue
     */
    @Bean
    public Queue queue(){
        return new ActiveMQQueue(queueName);
    }

    /**
     * 注入主题
     * @return Topic
     */
    public Topic topic(){
        return new ActiveMQTopic(topicName);
    }

}

````

#### 2.5.5 生产者

***queuq生产者***

````java
package com.maben.activemqSpringboot.produce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import java.util.UUID;

/**
 * activeMQ 生产者
 */
@Component
public class QueueProduce {
    @Autowired
    private JmsMessagingTemplate  jmsMessagingTemplate;
    @Autowired
    private Queue queue;

    public void productMessage(){
        jmsMessagingTemplate.convertAndSend(queue,"***springboot product message**"+ UUID.randomUUID().toString());
        System.out.println("**********发送消息完成************");
    }
}

````

***topic生产者***

````java
package com.maben.activemqSpringboot.produce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Topic;

/**
 * 配置topic的生产者
 */
@Component
public class TopicProduce {

    /**
     * 注入模板工具类
     */
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    /**
     * 注入topic的bean
     */
    @Autowired
    private Topic topic;

    public void sendTopicMsg(){
        jmsMessagingTemplate.convertAndSend(topic,"*******主题生产者*******");
    }

}
````



#### 2.5.6 消费者

````java
package com.maben.activemqSpringboot.consumer;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;

/**
 * activeMQ整合springboot队列消费者
 */
@Component
public class QueueConsumer {

    /**
     * 监听注解：监听相应的队列
     * @param textMessage textMessage
     * @throws Exception ..
     */
    @JmsListener(destination = "${my.queueName}")
    public void receive(TextMessage textMessage)throws Exception{
          System.out.println("************springboot接收消息********"+textMessage.getText());
    }

}

````

#### 2.5.7 测试类

````java
import com.maben.activemqSpringboot.ActivemqSpringbootMain;
import com.maben.activemqSpringboot.produce.QueueProduce;
import com.maben.activemqSpringboot.produce.TopicProduce;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * 测试消息MQ发送
 * 注意：测试哪一个在yml中修改相应的配置「pub-sub-domain」参数
 */
@SpringBootTest(classes = ActivemqSpringbootMain.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class TestActiveMQ {
    @Autowired
    private QueueProduce queueProduce;
    @Autowired
    private TopicProduce topicProduce;

    @Test
    public void testSendMsgQueue() throws Exception{
        queueProduce.productMessage();
    }
    @Test
    public void testSendMsgTopic()throws Exception{
        topicProduce.sendTopicMsg();
    }

}

````

#### 2.5.8 遗留问题

````java
springboot整合activeMQ，怎样两种方式同事使用；
````

### 2.6 activeMQ小知识

#### 2.6.1 通讯协议

##### 2.6.1.1 简单介绍

````
1.activeMQ支持的通讯协议有：TCP、NIO、UDP、SSL、HTTP（S）、VM。
2.配置位置：${activeMQ_home}/conf/activemq.xml中的<transportConnector>标签中；
  <transportConnectors>
    <!-- DOS protection, limit concurrent connections to 1000 and frame size to 100MB -->
    <transportConnector name="openwire" uri="tcp://0.0.0.0:61616? maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
    <transportConnector name="amqp" uri="amqp://0.0.0.0:5672?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
    <transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
    <transportConnector name="mqtt" uri="mqtt://0.0.0.0:1883?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
    <transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
  </transportConnectors>
3.详细看官网
	http://activemq.apache.org/configuring-version-5-transports.html
4.主要使用的也就是TCP和NIO的协议，其余的不太常用。
	注意：不同的协议代码可能不一样！！！
````

##### 2.6.1.2 TCP协议

***简介***

````
1.Transmission Control Protocol(TCP)是默认的。TCP的Client监听端口61616
2.TCP传输的优点：
	*可靠性高，稳定性强
	*高效（字节流方式传输，效率高）
	*有效性和可用性高，应用广泛，支持任何平台
````

***连接形式***

````
tcp://hostname:port?key=value
````

##### 2.6.1.3 NIO协议

***简介***

````
NIO协议与TCP协议类似，但是NIO协议更侧重于底层的访问操作；它允许开发人员对统一资源可有更多的client调用和服务端有更多的负载。
````

***使用场景***

````
*有大量的client去连到broker上；一般情况下，大量的client去连接broker是被操作系统的线程所限制的，因此NIO的实现比TCP需要更少的线程去运行，所以这种情况下建议使用NIO模式；
*可能对于broker有一个很迟钝的网络传输，也就是说NIO的性能要比TCP的要好！
````

***连接形式***

````
nio//ip:port?key=value
````

***配置文件***

````
修改activemq.xml配置文件
<broker>
  ...
  <transportConnectors>
    <transportConnector name="nio" uri="nio://0.0.0.0:61616"/>  
  </<transportConnectors>
  ...
</broker>
````

##### 2.6.1.4 协议扩展

````
现在的模式只能自己用自己的，也就是说指定tcp后面跟着tcp自己指定的端口，达不到通用的效果；
使用「auto+」的扩展模式就可以达到一中模式，多种协议共同使用的效果；
````

````
<transportConnector name="auto+nio" uri="auto+nio://0.0.0.0:61608?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600&amp;org.apache.activemq.transport.nio.SelectorManager.corePoolSize=20&amp;org.apache.activemq.transport.nio.Se1ectorManager.maximumPoo1Size=50"/>

**********************
也就是说，当代码中想使用「TCP」协议的时候只用如下URL:
	private static final String ACTIVEMQ_URL = "tcp://118.24.20.3:61608";
当代码想使用「NIO」协议的时候，URL改成下面这样就可以：
	private static final String ACTIVEMQ_URL = "nio://118.24.20.3:61608";
````

#### 2.6.2 消息存储和持久化

***简介***

````
1.持久化机制是什么？
	如果activeMQ出现宕机，消息不会丢失的机制。
2.持久化机制的存储逻辑是什么？
	activeMQ持久化机制有JDBC\AMQ\KAHADB\LEVELDB等，无论哪种持久化机制，消息的存储逻辑是一直的。就是在发送者将消息发送出去后，消息中心首先将消息存储到本地数据文件、内存数据库或者远程数据库等。再试图将消息发给接收者，成功则将消息从存储中删除，失败则继续尝试尝试发送。消息中心启动以后，要先检查指定的存储位置是否有未成功发送的消息，如果有，则会先把存储位置中的消息发出去。
````

***存储机制介绍***

````
--- AMQ MESSAGE STORE ---
AMQ是一种文件存储形式，他具有写入速度快和容易恢复的特点。消息存储在一个个文件中，文件的默认大小为32M，当一个存储文件中的全部消息已被消费，那么这个文件将被标识为可删除的，在下一个清除阶段，该文件将被删除。
AMQ使用于activeMQ5.3以前的版本，现在的已经不再使用了。
````

````
--- kahadb（重要） ---
1.kahadb是activeMQ目前来说的默认机制，可用于任何场景，提高了性能和恢复力；
2.消息存储机制使用的是「事物日志」和仅仅用一个索引文件来存储他所有的地址；数据被追加到「data、logs」中，当不再需要日志文件的使用，log文件就会被丢弃。
3.在activemq.xml配置文件中的写法：
	<persistenceAdapter>
         <kahaDB directory="${activemq.data}/kahadb"/>
   </persistenceAdapter>
4.kahadb存储目录：
	4.1 db-1.log ：（重要）
		kahadb存储消息到预定义大小（默认是32M）的数据记录文件中，文件名为db-<number>.log中；
		当一个文件已满时，就会创建新的日志文件，number加一；
		当日志文件中的消息都已被消费完成后，这些log文件也将会被丢弃；
  4.2 db.data ：（重要）
  	该文件包含了持久化的BTree索引，索引了消息记录的消息，他是消息的索引文件。
  4.3 db.free ：
  	当前db.data文件里哪些是空闲的，文件具体内容是所有空闲页面的id
  4.4 db.redo ：
    用来进行消息回复，如果kahadb消息存储在强制退出后启动，用户回复BTree索引。
  4.5 lock :
    文件锁，表示当前获得kahadb读写权限的broker；
````



