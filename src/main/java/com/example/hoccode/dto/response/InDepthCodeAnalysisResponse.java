package com.example.hoccode.dto.response;

import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InDepthCodeAnalysisResponse {
    ProblemAnalysis problem_analysis;
    List<Attempt> attempts;
    ProgressionSummary progression_summary;
    List<String> recommendations;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProblemAnalysis{
    String inferred_problem_type;
    List<String> key_concepts;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Attempt{
    Integer attempt_number;
    String approach;
    Complexity complexity;
    List<String> strengths;
    List<String> weaknesses;

    @Nullable
    List<String> improvements_from_previous;
}
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Complexity{
    String time;
    String space;
}
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProgressionSummary{
    List<String> key_insights;
    Integer best_attempt;
    String best_attempt_reason;
}