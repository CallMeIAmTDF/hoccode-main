package com.example.hoccode.controller;

import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    EmailService emailService;
    Executor taskExecutor;

    @KafkaListener(topics = "notification-delivery")
    public void listenNotificationDelivery(NotificationEvent message){
        emailService.sendEmailRegister(message);
    }

    @KafkaListener(topics = "forgot-password")
    public void listenNotificationForgotPassword(NotificationEvent message){
        emailService.sendEmailForgotPassword(message);
    }

    @KafkaListener(topics = "contest-delivery", concurrency = "3")
    public void listenContestDelivery(NotificationEvent message){
        CompletableFuture.runAsync(() -> {
            log.info("Sending email to {}", message.getRecipient());
            emailService.sendEmailContest(message);
        }, taskExecutor);
    }

    @KafkaListener(topics = "birthday")
    public void listenNotificationHappyBirthday(NotificationEvent message){
        emailService.sendEmailHappyBirthday(message);
    }

    @KafkaListener(topics = "streaks")
    public void listenNotificationStreaks(NotificationEvent message){
        emailService.sendEmailStreaks(message);
    }

    @KafkaListener(topics = "plagiarism")
    public void listenNotificationPlagiarism(NotificationEvent message){
        emailService.sendEmailPlagiarism(message);
    }
}