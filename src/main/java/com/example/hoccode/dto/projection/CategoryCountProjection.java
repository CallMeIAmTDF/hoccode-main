package com.example.hoccode.dto.projection;

import com.example.hoccode.util.constant.ProblemCategoryEnum;

public interface CategoryCountProjection {
    ProblemCategoryEnum getCategory();
    long getCount();
}