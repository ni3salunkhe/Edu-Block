package com.edu.servicesImpl;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.FeedbackIntelligenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class FeedbackIntelligenceServiceImpl implements FeedbackIntelligenceService {

    private final ReviewerLearningSignalRepository learningSignalRepository;
    private final RulePerformanceMetricRepository rulePerformanceMetricRepository;
    private final RecommendationPerformanceMetricRepository recommendationPerformanceMetricRepository;

    public FeedbackIntelligenceServiceImpl(ReviewerLearningSignalRepository learningSignalRepository,
                                           RulePerformanceMetricRepository rulePerformanceMetricRepository,
                                           RecommendationPerformanceMetricRepository recommendationPerformanceMetricRepository) {
        this.learningSignalRepository = learningSignalRepository;
        this.rulePerformanceMetricRepository = rulePerformanceMetricRepository;
        this.recommendationPerformanceMetricRepository = recommendationPerformanceMetricRepository;
    }

    @Override
    @Transactional
    public List<RulePerformanceMetric> aggregateRulePerformance(Instant start, Instant end) {
        List<ReviewerLearningSignal> signals = learningSignalRepository.findByCreatedAtBetween(start, end);
        Map<String, RuleAccumulator> ruleStats = new HashMap<>();

        for (ReviewerLearningSignal signal : signals) {
            // Process triggered verification rule codes
            for (String code : signal.getTriggeredRuleCodes()) {
                ruleStats.computeIfAbsent(code, k -> new RuleAccumulator("RULE"))
                        .addEvaluation(signal);
            }
            // Process triggered fraud signal codes
            for (String code : signal.getTriggeredFraudSignalCodes()) {
                ruleStats.computeIfAbsent(code, k -> new RuleAccumulator("FRAUD_SIGNAL"))
                        .addEvaluation(signal);
            }
        }

        List<RulePerformanceMetric> metrics = new ArrayList<>();
        for (Map.Entry<String, RuleAccumulator> entry : ruleStats.entrySet()) {
            RuleAccumulator acc = entry.getValue();
            RulePerformanceMetric metric = new RulePerformanceMetric();
            metric.setRuleName(entry.getKey());
            metric.setTargetType(acc.targetType);
            metric.setTotalEvaluations(acc.evals);
            metric.setOverrideCount(acc.overrides);
            metric.setTruePositives(acc.tp);
            metric.setFalsePositives(acc.fp);
            metric.setFalseNegatives(acc.fn);

            double precision = (acc.tp + acc.fp) > 0 ? (double) acc.tp / (acc.tp + acc.fp) : 1.0;
            metric.setPrecisionRate(precision);

            double overrideRate = acc.evals > 0 ? (double) acc.overrides / acc.evals : 0.0;
            metric.setOverrideRate(overrideRate);

            metric.setMetricsWindowStart(start);
            metric.setMetricsWindowEnd(end);

            metrics.add(rulePerformanceMetricRepository.save(metric));
        }

        return metrics;
    }

    @Override
    @Transactional
    public List<RecommendationPerformanceMetric> aggregateRecommendationPerformance(Instant start, Instant end) {
        List<ReviewerLearningSignal> signals = learningSignalRepository.findByCreatedAtBetween(start, end);
        Map<String, RecAccumulator> recStats = new HashMap<>();
        recStats.put("APPROVE", new RecAccumulator("APPROVE"));
        recStats.put("REVIEW", new RecAccumulator("REVIEW"));
        recStats.put("REJECT", new RecAccumulator("REJECT"));

        for (ReviewerLearningSignal signal : signals) {
            String type = signal.getRecommendedAction();
            if (recStats.containsKey(type)) {
                recStats.get(type).addEvaluation(signal);
            }
        }

        List<RecommendationPerformanceMetric> metrics = new ArrayList<>();
        for (Map.Entry<String, RecAccumulator> entry : recStats.entrySet()) {
            RecAccumulator acc = entry.getValue();
            RecommendationPerformanceMetric metric = new RecommendationPerformanceMetric();
            metric.setRecommendationType(entry.getKey());
            metric.setTotalRecommendations(acc.total);
            metric.setAcceptedRecommendations(acc.accepted);
            metric.setOverriddenRecommendations(acc.overridden);

            double accRate = acc.total > 0 ? (double) acc.accepted / acc.total : 0.0;
            double overRate = acc.total > 0 ? (double) acc.overridden / acc.total : 0.0;
            double precRate = acc.total > 0 ? (double) acc.accepted / acc.total : 0.0; // For category precision (agreed percentage)

            metric.setAcceptanceRate(accRate);
            metric.setOverrideRate(overRate);
            metric.setPrecisionRate(precRate);
            metric.setMetricsWindowStart(start);
            metric.setMetricsWindowEnd(end);

            metrics.add(recommendationPerformanceMetricRepository.save(metric));
        }

        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> detectSystematicMistakes() {
        List<RulePerformanceMetric> latestRules = rulePerformanceMetricRepository.findLatestMetrics();
        List<RecommendationPerformanceMetric> latestRecs = recommendationPerformanceMetricRepository.findLatestMetrics();
        List<String> alerts = new ArrayList<>();

        for (RulePerformanceMetric metric : latestRules) {
            if (metric.getTotalEvaluations() >= 30 && metric.getOverrideRate() > 0.20) {
                alerts.add(String.format("High override rate on %s '%s': overridden in %.1f%% of cases (%d/%d evaluations). Rule config may need calibration.",
                        metric.getTargetType(), metric.getRuleName(), metric.getOverrideRate() * 100, metric.getOverrideCount(), metric.getTotalEvaluations()));
            }

            if ("FRAUD_SIGNAL".equals(metric.getTargetType()) && metric.getTotalEvaluations() >= 10) {
                double fpRate = (double) metric.getFalsePositives() / metric.getTotalEvaluations();
                if (fpRate > 0.15) {
                    alerts.add(String.format("Fraud signal '%s' triggers false positives frequently (%.1f%%, %d/%d evaluations). Verification model accuracy may be compromised.",
                            metric.getRuleName(), fpRate * 100, metric.getFalsePositives(), metric.getTotalEvaluations()));
                }
            }
        }

        for (RecommendationPerformanceMetric metric : latestRecs) {
            if (metric.getTotalRecommendations() >= 20 && metric.getOverrideRate() > 0.15) {
                alerts.add(String.format("Recommendation type '%s' has low agreement rate: overridden in %.1f%% of reviews (%d/%d recommendations). Calibration required.",
                        metric.getRecommendationType(), metric.getOverrideRate() * 100, metric.getOverriddenRecommendations(), metric.getTotalRecommendations()));
            }
        }

        return alerts;
    }

    private static class RuleAccumulator {
        final String targetType;
        long evals = 0;
        long overrides = 0;
        long tp = 0; // True Positive (verifier agreed and rejected or reviewed)
        long fp = 0; // False Positive (rule reduced score but verifier approved anyway)
        long fn = 0; // False Negative (rule increased score but verifier rejected anyway)

        RuleAccumulator(String targetType) {
            this.targetType = targetType;
        }

        void addEvaluation(ReviewerLearningSignal s) {
            evals++;
            if (s.getIsOverride()) {
                overrides++;
                if ("REJECTED".equals(s.getActualDecision())) {
                    fn++;
                } else {
                    fp++;
                }
            } else {
                if ("REJECTED".equals(s.getActualDecision())) {
                    tp++;
                }
            }
        }
    }

    private static class RecAccumulator {
        final String type;
        long total = 0;
        long accepted = 0;
        long overridden = 0;

        RecAccumulator(String type) {
            this.type = type;
        }

        void addEvaluation(ReviewerLearningSignal s) {
            total++;
            if (Boolean.TRUE.equals(s.getIsOverride())) {
                overridden++;
            } else {
                accepted++;
            }
        }
    }
}
