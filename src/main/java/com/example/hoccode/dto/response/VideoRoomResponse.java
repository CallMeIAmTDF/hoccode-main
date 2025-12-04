package com.example.hoccode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRoomResponse {
    private String roomId;
    private String creatorEmail;
    private LocalDateTime createdAt;
}