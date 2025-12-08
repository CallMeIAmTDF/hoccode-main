package com.example.hoccode.config.listener;

import com.example.hoccode.dto.event.SubmissionCreatedEvent;
import com.example.hoccode.service.IUltraRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionEventListener {
    private final IUltraRecommendationService cfService;

    @EventListener
    public void handleNewSubmission(SubmissionCreatedEvent event) {
        cfService.onNewSubmission(event.getUserEmail(), event.getProblemId());
    }
}

