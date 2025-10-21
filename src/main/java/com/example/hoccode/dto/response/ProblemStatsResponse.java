package com.example.hoccode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemStatsResponse {
    private ProblemInfo problem;
    private long submissionCount;
    private long solvedCount;
    private double averageScore;
    private double acceptanceRate;

    public ProblemStatsResponse(
            Integer problemId,
            String title,
            String difficulty,
            long submissionCount,
            long solvedCount,
            double averageScore,
            double rawAcceptanceRate
    ) {
        this.problem = new ProblemInfo(problemId, title, difficulty);
        this.submissionCount = submissionCount;
        this.solvedCount = solvedCount;
        this.averageScore = roundOneDecimal(averageScore);
        this.acceptanceRate = roundOneDecimal(rawAcceptanceRate);
    }

    private double roundOneDecimal(double value) {
        BigDecimal bd = BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
        return bd.stripTrailingZeros().doubleValue();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProblemInfo {
        private Integer id;
        private String title;
        private String difficulty;
    }
}