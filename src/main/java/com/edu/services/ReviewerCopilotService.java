package com.edu.services;

import com.edu.entity.ReviewerFeedback;
import com.edu.entity.ReviewerRecommendation;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReviewerCopilotService {
    ReviewerRecommendation generateRecommendation(UUID documentId);
    ReviewerRecommendation getActiveRecommendation(UUID documentId);
    ReviewerRecommendation refreshRecommendation(UUID documentId);
    ReviewerFeedback saveFeedback(UUID recommendationId, UUID verifierId, String actualDecision, String comment, String overrideReason);
    Map<String, Object> getCopilotStatistics();
    List<ReviewerRecommendation> getRecommendationsHistory(UUID documentId);

    // (B) Workload Analytics timestamp setters
    ReviewerRecommendation markOpened(UUID documentId);   // Sets openedAt on first view
    ReviewerRecommendation markCompleted(UUID documentId); // Sets completedAt when feedback submitted

    // (D) Drift Analytics — AI-5 backbone
    Map<String, Object> getDriftAnalytics();
}
