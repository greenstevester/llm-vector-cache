package com.example.llmcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LlmVectorCacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(LlmVectorCacheApplication.class, args);
    }
}
