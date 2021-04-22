package com.maben.rocketmq_springboot.basic;

import com.maben.rocketmq_springboot.config.ExtRocketMQTemplate;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RocketMQ 消息发送端
 *  注意：事务监听器定义为MyTransactionListener
 */
@Component
public class MQProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private ExtRocketMQTemplate extRocketMQTemplate;

    /**
     * 发送普通消息
     * @param topic topic
     * @param message message
     * @throws Exception
     */
    public void sendMessage(String topic, String message) throws Exception {
        rocketMQTemplate.convertAndSend(topic, message);
    }

    /**
     * 发送事物1消息
     * @param topic topic
     * @param message message
     * @throws Exception
     */
    public void sendTransaction1Message(String topic, String message) throws Exception {
        String[] tags = { "TagA", "TagB", "TagC", "TagD", "TagE" };
        for (int i = 0; i < 10; i++) {
            try {
                Message msg = MessageBuilder
                        .withPayload(message + "-111-" + i) //设置消息
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, "KEY_" + i)//设置请求头
                        .build();
                SendResult sendResult = rocketMQTemplate.sendMessageInTransaction(
                        topic + ":" + tags[i % tags.length], msg, null);
                System.out.printf("send Transactional111 msg body = %s , sendResult=%s %n", msg.getPayload(), sendResult.getSendStatus());
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送事物2消息
     * @param topic topic
     * @param message message
     * @throws Exception
     */
    public void sendTransaction2Message(String topic, String message) throws Exception {
        String[] tags = { "TagA", "TagB", "TagC", "TagD", "TagE" };
        for (int i = 0; i < 10; i++) {
            try {
                Message msg = MessageBuilder
                        .withPayload(message + "-222-" + i) //设置消息
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, "KEY_" + i)//设置请求头
                        .build();
                SendResult sendResult = extRocketMQTemplate.sendMessageInTransaction(
                        topic + ":" + tags[i % tags.length], msg, null);
                System.out.printf("send Transactional222 msg body = %s , sendResult=%s %n", msg.getPayload(), sendResult.getSendStatus());
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
