package com.maben.activemqSpringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ActivemqSpringbootMain {


    public static void main(String[] args)throws Exception{

        SpringApplication.run(ActivemqSpringbootMain.class,args);
        System.out.println("****************启动完成！！！******************");

    }
}
