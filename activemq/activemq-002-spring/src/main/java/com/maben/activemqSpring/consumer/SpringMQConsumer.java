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
