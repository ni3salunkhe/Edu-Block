package com.edu.dto.events;

import java.util.UUID;

public class ReviewerFeedbackSubmittedEvent {
    private final UUID feedbackId;
    private final UUID recommendationId;

    public ReviewerFeedbackSubmittedEvent(UUID feedbackId, UUID recommendationId) {
        this.feedbackId = feedbackId;
        this.recommendationId = recommendationId;
    }

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public UUID getRecommendationId() {
        return recommendationId;
    }
}
