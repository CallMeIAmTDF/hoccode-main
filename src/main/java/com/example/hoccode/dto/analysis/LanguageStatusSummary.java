package com.example.hoccode.dto.analysis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LanguageStatusSummary {
    private long passed;
    private long failed;
    private long total;
}