package com.example.hoccode.config.scheduler;

import com.example.hoccode.config.template.RabbitKafkaTemplate;
import com.example.hoccode.entity.Streak;
import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.repository.StreakRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StreakScheduler {
    StreakRepository streakRepository;

    @Value("${api.base-url}")
    @NonFinal
    String baseUrl;

    @Value("${api.path.noti-streak}")
    @NonFinal
    String urlNoticeStreak;

    RabbitKafkaTemplate kafkaTemplate;

    @Scheduled(cron = "0 0 20 * * ?")
    public void StreakSchedulerJob() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        List<Streak> streaks = streakRepository.findNotUpdatedToday(todayStart);
        if(streaks.isEmpty()) return;
        streaks.forEach(streak -> {
            if(streak.getCurrentStreak() == 0) return;
            NotificationEvent event = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(String.join(",", streak.getUser().getEmail()))
                    .subject("Streaks Reminder")
                    .param(Map.of(
                            "name", streak.getUser().getName(),
                            "streakDays", streak.getCurrentStreak().toString(),
                            "continueUrl", baseUrl + urlNoticeStreak)
                    )
                    .build();
            kafkaTemplate.send("streaks", event);
        });

    }
}
