package com.example.hoccode.dto.request.update;

import com.example.hoccode.dto.request.create.CreateTestCaseRequest;
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
public class UpdateContestProblemRequest {
    Integer pid;
    String title;
    String description;
    String constrains;
    String followUp;
    Integer timeLimit;
    Integer memoryLimit;
    DifficultyEnum difficulty;
    Integer point;
    List<String> hints;
    List<CreateTestCaseRequest> testCases;
    List<ProblemCategoryEnum> categories;
}
