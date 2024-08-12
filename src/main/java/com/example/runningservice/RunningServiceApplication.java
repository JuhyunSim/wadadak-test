package com.example.runningservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RunningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunningServiceApplication.class, args);
    }
}
