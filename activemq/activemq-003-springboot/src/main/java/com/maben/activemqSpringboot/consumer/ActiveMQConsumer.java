package com.maben.activemqSpringboot.consumer;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;

/**
 * activeMQ整合springboot队列消费者
 */
@Component
public class ActiveMQConsumer {

    /**
     * 监听注解：监听queue相应的队列
     * @param textMessage textMessage
     * @throws Exception ..
     */
    @JmsListener(destination = "${my.queueName}")
    public void receiveQueue(TextMessage textMessage)throws Exception{
          System.out.println("************springboot queue接收消息:"+textMessage.getText());
    }

    @JmsListener(destination = "${my.topicName}")
    public void receiveTopic(TextMessage textMessage)throws Exception{
        System.out.println("***********springboot topic接收消息："+textMessage.getText());
    }

}
