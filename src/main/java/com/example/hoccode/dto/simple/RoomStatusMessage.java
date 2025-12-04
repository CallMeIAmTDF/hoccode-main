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
public class RoomStatusMessage {
    private String roomId;
    private String type; // user-joined, user-left, room-ready
    private String email;
    private int participantCount;
    private LocalDateTime timestamp;
}