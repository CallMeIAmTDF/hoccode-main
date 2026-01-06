package com.example.hoccode.config.template;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitKafkaTemplate {
    private final RabbitTemplate rabbitTemplate;
    public void send(String topic, Object message) {
        rabbitTemplate.convertAndSend(topic, message);
    }
}