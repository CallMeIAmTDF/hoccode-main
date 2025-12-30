package com.example.hoccode.dto.response.call;

import com.example.hoccode.util.constant.DifficultyEnum;
import com.example.hoccode.util.constant.ProblemCategoryEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeetCodeResponse {
    String title;
    String description;
    String constrains;
    String followUp;
    Integer timeLimit;
    Integer memoryLimit;

    DifficultyEnum difficulty;
    Boolean isContestProblem;
    Integer point;
    List<ProblemCategoryEnum> categories;
    List<Hint> hints;
    List<TestCase> testCases;

    @Data
    @Builder
    static class Hint{
        String description;
    }

    @Data
    @Builder
    static class TestCase{
        String input;
        String expectedOutput;
        Boolean isHidden;
    }
}
