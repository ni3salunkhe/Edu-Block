package com.edu.services;

import com.edu.entity.RulePerformanceMetric;
import com.edu.entity.RecommendationPerformanceMetric;
import java.time.Instant;
import java.util.List;

public interface FeedbackIntelligenceService {
    List<RulePerformanceMetric> aggregateRulePerformance(Instant start, Instant end);
    List<RecommendationPerformanceMetric> aggregateRecommendationPerformance(Instant start, Instant end);
    List<String> detectSystematicMistakes();
}
