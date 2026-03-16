package com.easymeeting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@MapperScan(basePackages = "com.easymeeting.mappers")
public class EasymeetingApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasymeetingApplication.class, args);
    }
}
