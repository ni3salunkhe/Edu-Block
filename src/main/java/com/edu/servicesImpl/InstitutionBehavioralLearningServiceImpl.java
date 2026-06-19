package com.edu.servicesImpl;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.InstitutionBehavioralLearningService;
import com.edu.services.InstitutionTrustService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class InstitutionBehavioralLearningServiceImpl implements InstitutionBehavioralLearningService {

    private final ReviewerLearningSignalRepository learningSignalRepository;
    private final InstitutionBehavioralTrendRepository trendRepository;
    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final InstitutionTrustService trustService;

    public InstitutionBehavioralLearningServiceImpl(ReviewerLearningSignalRepository learningSignalRepository,
                                                     InstitutionBehavioralTrendRepository trendRepository,
                                                     InstitutionTrustProfileRepository trustProfileRepository,
                                                     InstitutionTrustService trustService) {
        this.learningSignalRepository = learningSignalRepository;
        this.trendRepository = trendRepository;
        this.trustProfileRepository = trustProfileRepository;
        this.trustService = trustService;
    }

    @Override
    @Transactional
    public List<InstitutionBehavioralTrend> analyzeTrends(Instant start, Instant end) {
        List<InstitutionTrustProfile> profiles = trustProfileRepository.findAll();
        List<InstitutionBehavioralTrend> trends = new ArrayList<>();

        for (InstitutionTrustProfile profile : profiles) {
            UUID instId = profile.getInstitutionId();
            List<ReviewerLearningSignal> signals = learningSignalRepository.findByInstitutionIdAndCreatedAtAfter(instId, start);

            if (signals.isEmpty()) {
                continue;
            }

            long total = signals.size();
            long overrides = signals.stream().filter(ReviewerLearningSignal::getIsOverride).count();
            double overrideRate = (double) overrides / total;

            long fraudCount = signals.stream()
                    .mapToLong(s -> s.getTriggeredFraudSignalCodes().size())
                    .sum();

            // M2 requirement: use averageConfidenceScore from stored AI-2 confidence values
            double avgConfidence = signals.stream()
                    .mapToDouble(ReviewerLearningSignal::getVerificationConfidenceScore)
                    .average()
                    .orElse(50.0);

            // L3 Drift Severity Model Mapping
            DriftSeverity severity = DriftSeverity.LOW;
            if (overrideRate > 0.25) {
                severity = DriftSeverity.CRITICAL;
            } else if (overrideRate >= 0.15) {
                severity = DriftSeverity.HIGH;
            } else if (overrideRate >= 0.10) {
                severity = DriftSeverity.MEDIUM;
            } else if (overrideRate >= 0.05) {
                severity = DriftSeverity.LOW;
            }

            // Trend Type Mapping
            InstitutionBehavioralTrend.TrendType type = InstitutionBehavioralTrend.TrendType.STABLE;
            Instant deviationTime = null;

            if (overrideRate >= 0.15) {
                type = InstitutionBehavioralTrend.TrendType.DEVIATING;
                deviationTime = Instant.now();
            } else if (overrideRate < 0.05 && fraudCount == 0 && total >= 10) {
                type = InstitutionBehavioralTrend.TrendType.IMPROVING;
            } else if (overrideRate >= 0.10 && fraudCount > 3) {
                type = InstitutionBehavioralTrend.TrendType.VOLATILE;
            }

            InstitutionBehavioralTrend trend = new InstitutionBehavioralTrend();
            trend.setInstitutionId(instId);
            trend.setTotalVerifications(total);
            trend.setOverrideRate(overrideRate);
            trend.setTrendType(type);
            trend.setDriftSeverity(severity);
            trend.setFraudSignalsCount(fraudCount);
            trend.setAverageConfidenceScore(avgConfidence);
            trend.setLastDeviationDetectedAt(deviationTime);
            trend.setMetricsWindowStart(start);
            trend.setMetricsWindowEnd(end);

            trends.add(trendRepository.save(trend));

            // Auto-trigger trust recalculation when institution trust behavior drifts significantly
            if (severity == DriftSeverity.HIGH || severity == DriftSeverity.CRITICAL) {
                triggerAdaptiveTrustCalibration(instId);
            }
        }
        return trends;
    }

    @Override
    @Transactional
    public void triggerAdaptiveTrustCalibration(UUID institutionId) {
        // Recalculates trust profile using AI-3 engine with trigger details
        trustService.recalculateTrustProfile(
                institutionId,
                "OVERRIDE_DRIFT",
                "High verifier override rate detected in AI-5 Learning Feedback loop."
        );
    }
}
