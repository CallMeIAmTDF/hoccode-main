package com.example.hoccode.service.impl;

import com.example.hoccode.dto.response.RoomValidationResponse;
import com.example.hoccode.dto.response.VideoRoomResponse;
import com.example.hoccode.service.IInterviewService;
import com.example.hoccode.service.redis.IInterviewRedis;
import com.example.hoccode.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InterviewService implements IInterviewService {

    IInterviewRedis redis;
    SecurityUtil securityUtil;
    @Override
    public VideoRoomResponse createRoom(){
        String roomId = UUID.randomUUID().toString();
        redis.saveRoom(roomId);
        return VideoRoomResponse.builder()
                .roomId(roomId)
                .createdAt(LocalDateTime.now())
                .creatorEmail(securityUtil.getCurrentUserLogin().get())
                .build();
    }

    @Override
    public RoomValidationResponse validateRoom(String roomId){
        return RoomValidationResponse.builder()
                .valid(redis.isRoomExist(roomId))
                .roomId(roomId)
                .userEmail(securityUtil.getCurrentUserLogin().get())
                .build();
    }
}
