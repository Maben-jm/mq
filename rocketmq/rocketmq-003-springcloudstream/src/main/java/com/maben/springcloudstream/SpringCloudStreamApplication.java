package com.maben.springcloudstream;

import com.maben.springcloudstream.base.MySink;
import com.maben.springcloudstream.base.MySource;
import com.maben.springcloudstream.myCommodLineRunner.CustomRunner;
import com.maben.springcloudstream.myCommodLineRunner.CustomRunnerWithTransactional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;

/**
 * 启动类
 * EnableBinding是说建立消息的通道
 *  source：发送消息
 *  sink：接收消息
 */
@EnableBinding(value = { MySource.class , MySink.class })
@SpringBootApplication
public class SpringCloudStreamApplication {

    public static void main(String[] args){
        SpringApplication.run(SpringCloudStreamApplication.class,args);
        System.out.println("**********启动成功！！！***********");
    }

    /**
     * 启动自测output1
     * @return customRunner bean
     */
    @Bean
    public CustomRunner customRunner() {
        return new CustomRunner("output1");
    }

    /**
     * 启动自测output2
     * @return customRunnerWithTransactional bean
     */
    @Bean
    public CustomRunnerWithTransactional customRunnerWithTransactional() {
        return new CustomRunnerWithTransactional();
    }

}
