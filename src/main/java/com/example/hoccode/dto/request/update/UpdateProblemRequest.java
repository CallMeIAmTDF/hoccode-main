package com.example.hoccode.dto.request.update;

import com.example.hoccode.util.constant.DifficultyEnum;
import com.example.hoccode.util.constant.ProblemCategoryEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProblemRequest {
    @NotBlank(message = "Tiêu đề là bắt buộc")
    String title;

    @NotBlank(message = "Mô tả là bắt buộc")
    String description;
    private String constrains;
    private String followUp;
    private Integer timeLimit;
    private Integer memoryLimit;
    private DifficultyEnum difficulty;
    List<ProblemCategoryEnum> categories;
    private Integer point;
    private Boolean isContestProblem;
    private List<UpdateTestcaseRequest> testCases;
    private List<UpdateHintRequest> hints;
}