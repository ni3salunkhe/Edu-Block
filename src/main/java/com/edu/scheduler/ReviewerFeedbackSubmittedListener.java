package com.edu.scheduler;

import com.edu.dto.events.ReviewerFeedbackSubmittedEvent;
import com.edu.services.ReviewerLearningService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReviewerFeedbackSubmittedListener {

    private final ReviewerLearningService learningService;

    public ReviewerFeedbackSubmittedListener(ReviewerLearningService learningService) {
        this.learningService = learningService;
    }

    @Async
    @EventListener
    public void handleFeedbackSubmittedEvent(ReviewerFeedbackSubmittedEvent event) {
        try {
            learningService.processFeedbackSignal(event.getFeedbackId());
        } catch (Exception e) {
            System.err.println("Failed to process reviewer feedback signal: " + e.getMessage());
        }
    }
}
