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
public class SignalMessage {
    private String type; // offer, answer, ice-candidate
    private String roomId;
    private String fromEmail;
    private String toEmail;
    private Object data;
    private LocalDateTime timestamp;
}