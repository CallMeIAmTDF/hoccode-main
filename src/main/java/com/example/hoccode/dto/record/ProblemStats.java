package com.example.hoccode.dto.record;


import java.util.Map;

public record ProblemStats(
        int solved,
        int total,
        Map<String, DifficultyStats> difficulty
) {}