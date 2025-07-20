package com.example.hoccode.dto.request;

import com.example.hoccode.util.constant.DifficultyEnum;
import com.example.hoccode.util.constant.ProblemCategoryEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProblemSearchRequest {
    private String keyword;
    private DifficultyEnum difficulty;
    private List<ProblemCategoryEnum> categories;
    private int page = 0;
    private int size = 20;
}