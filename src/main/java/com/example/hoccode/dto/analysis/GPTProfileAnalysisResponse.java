package com.example.hoccode.dto.analysis;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GPTProfileAnalysisResponse {
    private Overview overview;
    private Insights insights;
    private String[] highlights;
    private AchievementScore achievementScore;
    private String[] funFacts;
    private ImprovementSuggestions improvementSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Overview {
        private int totalSubmissions;
        private String passRateDescription;
        private String dominantLanguage;
        private String strongestCategory;
        private String activeTimeSlot;
        private String activeDay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Insights {
        private String studyHabits;
        private String languageAnalysis;
        private String categoryAnalysis;
        private String behaviorPrediction;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AchievementScore {
        private String consistency;
        private String problemSolving;
        private String languageMastery;
        private String overall;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ImprovementSuggestions {
        private String weakCategories;
        private String languageGrowth;
        private String timeOptimization;
        private String skillExpansion;
    }
}
