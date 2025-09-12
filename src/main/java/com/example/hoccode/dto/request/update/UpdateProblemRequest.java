package com.example.hoccode.dto.request.update;

import com.example.hoccode.util.constant.DifficultyEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProblemRequest {
    private String title;
    private String description;
    private String constrains;
    private String followUp;
    private Integer timeLimit;
    private Integer memoryLimit;
    private DifficultyEnum difficulty;
    private Integer point;
    private Boolean isContestProblem;
    private List<UpdateTestcaseRequest> testCases;
    private List<UpdateHintRequest> hints;
}