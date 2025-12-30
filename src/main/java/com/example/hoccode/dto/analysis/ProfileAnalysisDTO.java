package com.example.hoccode.dto.analysis;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileAnalysisDTO {
    private SubmissionSummary submissionSummary;
    private Map<String, Long> dailyTimeSlotAnalysis;
    private DayOfWeekAnalysis dayOfWeekAnalysis;
    private Map<String, Long> categorySubmissionCount;
    private Map<String, LanguageStatusSummary> languageSubmissionSummary;
    private Stats stats;

    @Data
    @Builder
    public static class SubmissionSummary {
        private Long passedSubmissions;
        private Long failedSubmissions;
    }

    @Data
    @Builder
    public static class DayOfWeekAnalysis {
        private String busiestDay;
        private String busiestTimeSlot;
    }


    @Data
    @Builder
    public static class Stats {
        private Integer rank;
        private Integer point;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime joined;
    }
}


