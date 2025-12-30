package com.example.hoccode.dto.response;

import com.example.hoccode.util.constant.StatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MySubmissionResponse {
    String id;
    Integer pid;
    String code;
    String language;
    StatusEnum status;
    Integer totalTestCases;
    Integer passedTestCase;
    Integer maxTime;
    private LocalDateTime createdAt;
    boolean isContest;
}
