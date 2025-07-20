package com.example.hoccode.dto.request.call;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CodeRunnerRequest {
    String language;
    String code;

    @JsonProperty("time_limit")
    Integer timeLimit;

    @JsonProperty("memory_limit")
    Integer memoryLimit;

    @JsonProperty("submission_id")
    String submissionId;

    @JsonProperty("problem_id")
    Integer problemId;

    @JsonProperty("user_id")
    Integer userId;

    List<CallTestCaseRequest> testcases;
}