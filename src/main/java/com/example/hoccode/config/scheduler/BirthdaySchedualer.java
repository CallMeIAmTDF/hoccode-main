package com.example.hoccode.config.scheduler;

import com.example.hoccode.entity.User;
import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BirthdaySchedualer {
    UserRepository userRepository;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(cron = "0 0 8 * * ?")
    public void BirthdaySchedulerJob() {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findUsersByBirthday(today.getDayOfMonth(), today.getMonthValue());

        users.forEach(user -> {
            NotificationEvent event = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(String.join(",", user.getEmail()))
                    .subject("Happy Your Birthday")
                    .param(Map.of(
                            "name", user.getName()
                    ))
                    .build();
            kafkaTemplate.send("birthday", event);
        });

    }
}
