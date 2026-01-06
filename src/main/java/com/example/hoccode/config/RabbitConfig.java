package com.example.hoccode.config;

import org.springframework.amqp.core.TopicExchange;
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

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification.exchange");
    }

    @Bean
    public Queue plagiarismQueue() {
        return new Queue("plagiarism", true);
    }

    @Bean
    public Queue contestDeliveryQueue() {
        return new Queue("contest-delivery", true);
    }

    @Bean
    public Queue streaksQueue() {
        return new Queue("streaks", true);
    }

    @Bean
    public Queue notificationDeliveryQueue() {
        return new Queue("notification-delivery", true);
    }

    @Bean
    public Queue forgotPasswordQueue() {
        return new Queue("forgot-password", true);
    }

    @Bean
    public Queue birthdayQueue() {
        return new Queue("birthday", true);
    }

    @Bean
    public Queue premiumQueue() {
        return new Queue("premium", true);
    }
}