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
    @Bean
    public Topic topic(){
        return new ActiveMQTopic(topicName);
    }

}
