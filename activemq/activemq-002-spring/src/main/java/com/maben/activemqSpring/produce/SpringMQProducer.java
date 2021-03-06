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
