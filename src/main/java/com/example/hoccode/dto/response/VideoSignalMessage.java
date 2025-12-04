package com.example.hoccode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSignalMessage {
    private String type; // "offer", "answer", "ice-candidate", "user-joined", "user-left"
    private String roomId;
    private String from;
    private Object data; // SDP / ICE
}