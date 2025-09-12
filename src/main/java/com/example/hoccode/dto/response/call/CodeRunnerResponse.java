package com.example.hoccode.dto.response.call;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CodeRunnerResponse {
    String message;
    Integer passed;
    String status;
    Integer total;
    List<Result> results;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

class Result {
    Integer case_id;
    String status;
    String message;
    Integer time;
}