package com.example.hoccode.config;

import com.example.hoccode.service.IUltraRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCachePreloader implements ApplicationListener<ApplicationReadyEvent> {
    final IUltraRecommendationService cfService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        cfService.ensureCacheInitialized();
    }

}