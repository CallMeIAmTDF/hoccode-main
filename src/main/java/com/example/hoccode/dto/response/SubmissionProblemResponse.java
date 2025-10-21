package com.example.hoccode.dto.response;

import com.example.hoccode.util.constant.DifficultyEnum;
import com.example.hoccode.util.constant.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionProblemResponse {
    private String id;
    private Integer pid;
    private SimpleProblemInfo problem;
    private String code;
    private String language;
    private StatusEnum status;
    private Integer totalTestCases;
    private Integer passedTestCase;
    private String message;
    private Integer maxTime;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public SubmissionProblemResponse(
            String id,
            Integer pid,
            SimpleProblemInfo problem,
            String code,
            String language,
            StatusEnum status,
            Integer totalTestCases,
            Integer passedTestCase,
            String message,
            Integer maxTime,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime updatedAt,
            String updatedBy
    ) {
        this.id = id;
        this.pid = pid;
        this.problem = problem;
        this.code = code;
        this.language = language;
        this.status = status;
        this.totalTestCases = totalTestCases;
        this.passedTestCase = passedTestCase;
        this.message = message;
        this.maxTime = maxTime;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }
}