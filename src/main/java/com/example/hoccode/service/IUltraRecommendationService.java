package com.example.hoccode.service;

import com.example.hoccode.entity.Problem;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface IUltraRecommendationService {
    Map<String, Integer> recommend();

    Problem recommendProblem();

    void ensureCacheInitialized();

    @Async
    void onNewSubmission(String userEmail, Integer problemId);

    void clearCache();
}
