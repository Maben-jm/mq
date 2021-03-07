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
