package com.maben.rocketmq001.rocketmq002_base.oneway;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.concurrent.TimeUnit;

/**
 * 发送单向消息
 */
public class OneWayProducer {
    public static void main(String[] args)throws Exception{
        final DefaultMQProducer producer = new DefaultMQProducer("group1");
        producer.setNamesrvAddr("localhost:9876");
        producer.start();
        for (int i = 0; i < 10; i++) {
            final Message msg = new Message("TopicTest" /* Topic */,
                    "Tag3" /* Tag */,
                    ("one way  " + i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
            );
            producer.sendOneway(msg);
            TimeUnit.SECONDS.sleep(1);
        }
        producer.shutdown();
    }
}