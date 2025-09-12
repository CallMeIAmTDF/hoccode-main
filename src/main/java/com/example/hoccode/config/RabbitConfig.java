package com.example.hoccode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue matchmakingQueue() {
        return new Queue("matchmaking-queue", true);
    }

    @Bean
    public Queue submissionResultQueue() {
        return new Queue("submission-result-queue", true);
    }
}