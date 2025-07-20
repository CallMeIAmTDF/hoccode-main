package com.example.hoccode.dto.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateContestRequest {
    private String title;
    private String description;
    private String password;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer numAttendees;
    private List<CreateProblemRequest> problems;
}