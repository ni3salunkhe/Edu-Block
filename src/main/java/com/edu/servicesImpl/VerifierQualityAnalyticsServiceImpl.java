package com.edu.servicesImpl;

import com.edu.entity.ReviewerLearningSignal;
import com.edu.entity.VerifierQualityMetrics;
import com.edu.repositories.ReviewerLearningSignalRepository;
import com.edu.repositories.VerifierQualityMetricsRepository;
import com.edu.services.VerifierQualityAnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class VerifierQualityAnalyticsServiceImpl implements VerifierQualityAnalyticsService {

    private final ReviewerLearningSignalRepository learningSignalRepository;
    private final VerifierQualityMetricsRepository verifierMetricsRepository;

    public VerifierQualityAnalyticsServiceImpl(ReviewerLearningSignalRepository learningSignalRepository,
                                               VerifierQualityMetricsRepository verifierMetricsRepository) {
        this.learningSignalRepository = learningSignalRepository;
        this.verifierMetricsRepository = verifierMetricsRepository;
    }

    @Override
    @Transactional
    public List<VerifierQualityMetrics> calculateVerifierMetrics(Instant start, Instant end) {
        List<ReviewerLearningSignal> signals = learningSignalRepository.findByCreatedAtBetween(start, end);
        if (signals.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, List<ReviewerLearningSignal>> byVerifier = new HashMap<>();
        for (ReviewerLearningSignal s : signals) {
            byVerifier.computeIfAbsent(s.getVerifierId(), k -> new ArrayList<>()).add(s);
        }

        double globalOverrideRate = signals.stream().filter(ReviewerLearningSignal::getIsOverride).count() / (double) signals.size();

        List<VerifierQualityMetrics> list = new ArrayList<>();
        for (Map.Entry<UUID, List<ReviewerLearningSignal>> entry : byVerifier.entrySet()) {
            UUID verifierId = entry.getKey();
            List<ReviewerLearningSignal> vSignals = entry.getValue();

            long total = vSignals.size();
            long overrides = vSignals.stream().filter(ReviewerLearningSignal::getIsOverride).count();
            double overrideRate = (double) overrides / total;
            double avgTime = vSignals.stream().mapToLong(ReviewerLearningSignal::getReviewTimeSeconds).average().orElse(0.0);

            // rubberStampingRisk: high verifier speed (< 15 seconds) combined with low overrides
            boolean rubberStamping = (avgTime < 15.0) && (overrideRate < 0.02) && (total >= 10);

            // Consensus agreement: how close the individual override rate is to the group mean override rate
            double variance = Math.abs(overrideRate - globalOverrideRate);
            double agreement = 1.0 - variance;

            VerifierQualityMetrics metric = new VerifierQualityMetrics();
            metric.setVerifierId(verifierId);
            metric.setTotalReviews(total);
            metric.setOverrideCount(overrides);
            metric.setOverrideRate(overrideRate);
            metric.setAverageReviewTimeSeconds(avgTime);
            metric.setConsensusAgreementRate(agreement);
            metric.setDecisionVariance(variance);
            metric.setRubberStampingRisk(rubberStamping);
            metric.setMetricsWindowStart(start);
            metric.setMetricsWindowEnd(end);

            list.add(verifierMetricsRepository.save(metric));
        }

        return list;
    }
}
