package com.example.hoccode.dto.response;

import com.example.hoccode.entity.Hint;
import com.example.hoccode.entity.TestCase;
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
public class ProblemResponse {
    Integer id;
    String title;
    String description;
    String constrains;
    String followUp;
    Integer timeLimit;
    Integer memoryLimit;
    DifficultyEnum difficulty;
    Integer point;
    private List<ProblemCategoryEnum> categories;
    private Boolean accepted;
    private List<Hint> hints;
    private List<TestCase> allTestCases;

    Long passCount;
    Long failCount;
}
