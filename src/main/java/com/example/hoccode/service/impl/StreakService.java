package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.entity.Streak;
import com.example.hoccode.entity.User;
import com.example.hoccode.repository.StreakRepository;
import com.example.hoccode.repository.UserRepository;
import com.example.hoccode.service.IStreakService;
import com.example.hoccode.util.constant.SocketType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StreakService implements IStreakService {
    StreakRepository streakRepository;
    UserRepository userRepository;
    SimpMessagingTemplate webSocketTemplate;

    @Override
    public void increment(String email) {
        User u = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("Email Not Found"));
        // tồn tại
        Streak streak = streakRepository.findFirstByUser(u);
        if(streak != null){
            LocalDate today = LocalDate.now();
            LocalDate lastDate = streak.getUpdatedAt() != null ? streak.getUpdatedAt().toLocalDate() : streak.getCreatedAt().toLocalDate();
            // đã update
            if(lastDate.equals(today)){
                return;
            }
            // ngày hôm qua
            else if(lastDate.equals(today.minusDays(1))){
                webSocketTemplate.convertAndSendToUser(
                        email,
                        "/topic/normal",
                        new SocketMessage(SocketType.STREAK_UPDATE, "STREAK_UPDATE",
                                streak.getCurrentStreak())
                );
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            }
            // mất chuỗi
            else {
                webSocketTemplate.convertAndSendToUser(
                        email,
                        "/topic/normal",
                        new SocketMessage(SocketType.STREAK_UPDATE, "STREAK_UPDATE",
                                0)
                );
                streak.setCurrentStreak(1);
            }
            if (streak.getCurrentStreak() > streak.getMaxStreak()) {
                streak.setMaxStreak(streak.getCurrentStreak());
            }
        }else{
            webSocketTemplate.convertAndSendToUser(
                    email,
                    "/topic/normal",
                    new SocketMessage(SocketType.STREAK_UPDATE, "STREAK_UPDATE",
                            0)
            );
            streak = Streak.builder()
                    .user(u)
                    .maxStreak(1)
                    .currentStreak(1)
                    .build();
        }
        streakRepository.save(streak);
    }

    @Override
    public Streak getStreak(String email) {
        User u = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("Email Not Found"));
        Streak streak = streakRepository.findFirstByUser(u);
        if(streak != null){
            LocalDate today = LocalDate.now();
            LocalDate lastDate = streak.getUpdatedAt() != null ? streak.getUpdatedAt().toLocalDate() : streak.getCreatedAt().toLocalDate();
            if(lastDate.isBefore(today.minusDays(1))){
                streak.setCurrentStreak(0);
                streakRepository.save(streak);
            }
        }else{
            streak = Streak.builder()
                    .user(u)
                    .maxStreak(0)
                    .currentStreak(0)
                    .build();
            streakRepository.save(streak);
        }
        return streak;
    }
}
