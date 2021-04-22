package com.maben.rocketmq_springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 */
@SpringBootApplication
public class SpringbootRocketmqApplication {
    public static void main(String[] args)throws Exception{
        SpringApplication.run(SpringbootRocketmqApplication.class,args);
        System.out.println("**************启动成功！！！************");
    }
}
