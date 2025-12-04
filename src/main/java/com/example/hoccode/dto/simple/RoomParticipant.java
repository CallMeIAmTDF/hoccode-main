package com.example.hoccode.dto.simple;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipant {
    private String email;
    private String roomId;
    private LocalDateTime joinedAt;
    private boolean isCreator;
}