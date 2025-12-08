package com.example.hoccode.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmissionCreatedEvent {
    private String userEmail;
    private Integer problemId;
}