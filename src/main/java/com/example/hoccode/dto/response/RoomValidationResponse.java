package com.example.hoccode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomValidationResponse {
    private boolean valid;
    private String roomId;
    private String userEmail;
}