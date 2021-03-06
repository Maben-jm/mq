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
