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
        System.out.println("***出题生产者发送成功！！！***");
    }

}
