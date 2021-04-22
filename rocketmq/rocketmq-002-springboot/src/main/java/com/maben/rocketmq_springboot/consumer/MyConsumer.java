package com.maben.rocketmq_springboot.consumer;

import com.maben.rocketmq_springboot.constant.MyConstant;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMQ Consumer
 */
@Component
@RocketMQMessageListener(consumerGroup = "MyConsumerGroup",topic = MyConstant.topic)
public class MyConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        System.out.println("received message is "+message);
    }
}
