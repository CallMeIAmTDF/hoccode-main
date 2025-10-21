package com.example.hoccode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentSubmissionResponse {
    private String username;
    private String problemName;
    private String status;
    private LocalDateTime submitTime;
}