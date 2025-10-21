package com.example.hoccode.dto.response;

import com.example.hoccode.util.constant.DifficultyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleProblemInfo {
    private Integer id;
    private String title;
    private DifficultyEnum difficulty;
}