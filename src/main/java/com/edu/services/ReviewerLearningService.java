package com.edu.services;

import com.edu.entity.ReviewerLearningSignal;
import java.util.List;
import java.util.UUID;

public interface ReviewerLearningService {
    ReviewerLearningSignal processFeedbackSignal(UUID feedbackId);
    List<ReviewerLearningSignal> getRecentSignals();
}
