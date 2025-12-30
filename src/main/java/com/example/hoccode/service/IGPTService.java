package com.example.hoccode.service;

import com.example.hoccode.dto.analysis.GPTProfileAnalysisResponse;
import com.example.hoccode.dto.response.InDepthCodeAnalysisResponse;
import com.example.hoccode.dto.response.call.BasicCodeAnalysisResponse;
import com.example.hoccode.dto.response.call.LeetCodeResponse;
import org.springframework.cache.annotation.Cacheable;

public interface IGPTService {

    BasicCodeAnalysisResponse basicCodeAnalysis(String submissionId);

    InDepthCodeAnalysisResponse inDepthCodeAnalysis(Integer problemId);

    GPTProfileAnalysisResponse profileSummary();

    LeetCodeResponse crawlLeetCode(String url);
}
