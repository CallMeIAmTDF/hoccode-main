package com.example.hoccode.dto.simple;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSimilarity {
    private String email;
    private double similarity;
}