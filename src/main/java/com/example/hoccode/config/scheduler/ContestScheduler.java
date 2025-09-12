package com.example.hoccode.config.scheduler;


import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.entity.Contest;
import com.example.hoccode.entity.ContestParticipant;
import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.repository.ContestParticipantRepository;
import com.example.hoccode.repository.ContestRepository;
import com.example.hoccode.service.redis.impl.StringStringIntegerRedis;
import com.example.hoccode.util.constant.SocketType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContestScheduler {
    ContestRepository contestRepository;
    SimpMessagingTemplate simpMessagingTemplate;
    StringStringIntegerRedis stringStringIntegerRedis;
    RedisTemplate<String, Boolean> redisTemplate;
    ContestParticipantRepository participantRepository;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedRate = 60000)
    public void checkUpcomingContests() {
        LocalDateTime now = LocalDateTime.now();

        // ====== REMINDER 1 HOUR BEFORE ======
        LocalDateTime oneHourLater = now.plusHours(1);
        List<Contest> upcoming = contestRepository.findByStartTimeBetweenAndDeletedFalse(now, oneHourLater);
        for (Contest contest : upcoming) {
            String redisKey = "contest:reminder:" + contest.getId();
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().get(redisKey))) continue;

            List<ContestParticipant> participants = participantRepository.findByContest(contest);
            List<String> emails = participants.stream()
                    .map(cp -> cp.getUser().getEmail())
                    .filter(Objects::nonNull)
                    .toList();
            NotificationEvent event = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(String.join(",", emails))
                    .subject("Contest Reminder")
                    .param(Map.of(
                            "code", contest.getId().toString(),
                            "title", contest.getTitle(),
                            "startTime", contest.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")),
                            "endTime", contest.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")),
                            "description", contest.getDescription()
                    ))
                    .build();
            kafkaTemplate.send("contest-delivery", event);
            redisTemplate.opsForValue().set(redisKey, true, Duration.ofHours(2));
        }

        // ====== STARTING NOW ======
        List<Contest> startingNow = contestRepository.findByStartTimeBetweenAndDeletedFalse(
                now.minusSeconds(30),
                now.plusSeconds(30)
        );

        for (Contest contest : startingNow) {
            String wsKey = "contest:ws:" + contest.getId();
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().get(wsKey))) continue;

            List<ContestParticipant> participants = participantRepository.findByContest(contest);

            participants.parallelStream()
                    .filter(cp -> cp != null && cp.getUser() != null && cp.getUser().getName() != null)
                    .forEach(cp -> {
                        SocketMessage payload = new SocketMessage(
                                SocketType.CONTEST,
                                "Contest " + contest.getTitle() + " is starting now",
                                Map.of("contestId", contest.getId(), "title", contest.getTitle())
                        );
                        simpMessagingTemplate.convertAndSendToUser(
                                cp.getUser().getEmail(),
                                "/queue/notification",
                                payload
                        );
                    });
            redisTemplate.opsForValue().set(wsKey, true, Duration.ofHours(1));
        }
    }
}
