package com.example.hoccode.dto.response;

import com.example.hoccode.util.constant.ProblemCategoryEnum;

public record CategoryCountResponse(
    ProblemCategoryEnum category,
    long count
) {}