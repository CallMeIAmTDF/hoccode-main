package com.example.hoccode.service;

import com.example.hoccode.dto.response.InDepthCodeAnalysisResponse;
import com.example.hoccode.dto.response.call.BasicCodeAnalysisResponse;

public interface IGPTService {

    BasicCodeAnalysisResponse basicCodeAnalysis(String submissionId);

    InDepthCodeAnalysisResponse inDepthCodeAnalysis(Integer problemId);
}
