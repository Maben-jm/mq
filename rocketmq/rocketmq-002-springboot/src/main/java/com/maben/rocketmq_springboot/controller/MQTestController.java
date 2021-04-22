package com.maben.rocketmq_springboot.controller;

import com.maben.rocketmq_springboot.basic.MQProducer;
import com.maben.rocketmq_springboot.constant.MyConstant;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * MQ Send Test
 */
@RestController
@RequestMapping("MQTest")
public class MQTestController {

    @Resource
    private MQProducer mqProducer;

    /**
     * send common message
     * @param message message
     * @return send result
     */
    @RequestMapping("/sendMessage")
    public String sendMessage(String message){
        try {
            mqProducer.sendMessage(MyConstant.topic,message);
        }catch (Exception e){
            e.printStackTrace();
            return "send error,message is "+e.getMessage();
        }
        return "send success";
    }

    /**
     * send transaction message
     * @param message message
     * @return send result
     */
    @RequestMapping("/sendTransactionMessage")
    public String sendTransactionMessage(String message){
        try {
            mqProducer.sendTransaction1Message(MyConstant.topic,message);
            mqProducer.sendTransaction2Message(MyConstant.topic,message);
        }catch (Exception e){
            e.printStackTrace();
            return "send error,message is "+e.getMessage();
        }
        return "send transaction message success.";
    }

}
