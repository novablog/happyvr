package com.happyvr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HappyVR平台主启动类
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HappyVrApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HappyVrApplication.class, args);
    }
}