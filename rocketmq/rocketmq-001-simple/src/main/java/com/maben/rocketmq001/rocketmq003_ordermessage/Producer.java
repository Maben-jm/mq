package com.maben.rocketmq001.rocketmq003_ordermessage;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 顺序消费生产者
 *  主要是通过select()方法自定义实现保证针对同一个orderId的消息，都是走相同的messageQueue；因为针对某一个messageQueue是有序的，以此来保证有序行。
 */
public class Producer {
    public static void main(String[] args) throws UnsupportedEncodingException {
        try {
            MQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
            ((DefaultMQProducer) producer).setNamesrvAddr("localhost:9876");
            producer.start();
            for (int i = 0; i < 10; i++) {
                final int orderId = i;
                for (int j = 0; j < 5; j++) {
                    Message msg = new Message("OrderTopicTest", "order_"+orderId, "KEY" + i, ("order_"+orderId+" step " + j).getBytes(RemotingHelper.DEFAULT_CHARSET));
                    SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                        @Override
                        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                            Integer orderId = (Integer) arg;
                            int index = orderId % mqs.size();
                            return mqs.get(index);
                        }
                    }, orderId);
                    System.out.printf("%s%n", sendResult);
                }
            }
            producer.shutdown();
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
