package com.edu.servicesImpl;

import com.edu.entity.InstitutionBehavioralTrend;
import com.edu.entity.ReviewerLearningSignal;
import com.edu.entity.RulePerformanceMetric;
import com.edu.repositories.InstitutionBehavioralTrendRepository;
import com.edu.repositories.ReviewerLearningSignalRepository;
import com.edu.repositories.RulePerformanceMetricRepository;
import com.edu.services.LearningDatasetExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class LearningDatasetExportServiceImpl implements LearningDatasetExportService {

    private final ReviewerLearningSignalRepository signalRepository;
    private final RulePerformanceMetricRepository ruleRepository;
    private final InstitutionBehavioralTrendRepository trendRepository;
    private final ObjectMapper objectMapper;

    public LearningDatasetExportServiceImpl(ReviewerLearningSignalRepository signalRepository,
                                           RulePerformanceMetricRepository ruleRepository,
                                           InstitutionBehavioralTrendRepository trendRepository,
                                           ObjectMapper objectMapper) {
        this.signalRepository = signalRepository;
        this.ruleRepository = ruleRepository;
        this.trendRepository = trendRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public String exportSignalsAsCsv(Instant start, Instant end) {
        List<ReviewerLearningSignal> list = signalRepository.findByCreatedAtBetween(start, end);
        StringBuilder sb = new StringBuilder();
        sb.append("id,recommendationId,documentId,institutionId,verifierId,recommendedAction,actualDecision,isOverride,overrideReason,decisionGap,reviewTimeSeconds,triggeredRuleCodes,triggeredFraudSignalCodes,verificationConfidenceScore,riskLevel,confidenceLevel,verificationEngineVersion,trustEngineVersion,copilotVersion,ruleConfigurationVersion,createdAt\n");
        
        for (ReviewerLearningSignal s : list) {
            sb.append(s.getId()).append(",")
              .append(s.getRecommendationId()).append(",")
              .append(s.getDocumentId()).append(",")
              .append(s.getInstitutionId() != null ? s.getInstitutionId().toString() : "").append(",")
              .append(s.getVerifierId()).append(",")
              .append(s.getRecommendedAction()).append(",")
              .append(s.getActualDecision()).append(",")
              .append(s.getIsOverride()).append(",")
              .append(s.getOverrideReason() != null ? s.getOverrideReason() : "").append(",")
              .append(s.getDecisionGap()).append(",")
              .append(s.getReviewTimeSeconds()).append(",")
              .append("\"").append(String.join(";", s.getTriggeredRuleCodes())).append("\",")
              .append("\"").append(String.join(";", s.getTriggeredFraudSignalCodes())).append("\",")
              .append(s.getVerificationConfidenceScore() != null ? s.getVerificationConfidenceScore() : "").append(",")
              .append(s.getRiskLevel() != null ? s.getRiskLevel() : "").append(",")
              .append(s.getConfidenceLevel() != null ? s.getConfidenceLevel() : "").append(",")
              .append(s.getVerificationEngineVersion() != null ? s.getVerificationEngineVersion() : "").append(",")
              .append(s.getTrustEngineVersion() != null ? s.getTrustEngineVersion() : "").append(",")
              .append(s.getCopilotVersion() != null ? s.getCopilotVersion() : "").append(",")
              .append(s.getRuleConfigurationVersion() != null ? s.getRuleConfigurationVersion() : "").append(",")
              .append(s.getCreatedAt()).append("\n");
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportSignalsAsJson(Instant start, Instant end) {
        List<ReviewerLearningSignal> list = signalRepository.findByCreatedAtBetween(start, end);
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String exportRulePerformanceAsCsv() {
        List<RulePerformanceMetric> list = ruleRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,ruleName,targetType,totalEvaluations,overrideCount,truePositives,falsePositives,falseNegatives,precisionRate,overrideRate,metricsWindowStart,metricsWindowEnd\n");
        
        for (RulePerformanceMetric m : list) {
            sb.append(m.getId()).append(",")
              .append(m.getRuleName()).append(",")
              .append(m.getTargetType()).append(",")
              .append(m.getTotalEvaluations()).append(",")
              .append(m.getOverrideCount()).append(",")
              .append(m.getTruePositives()).append(",")
              .append(m.getFalsePositives()).append(",")
              .append(m.getFalseNegatives()).append(",")
              .append(m.getPrecisionRate()).append(",")
              .append(m.getOverrideRate()).append(",")
              .append(m.getMetricsWindowStart()).append(",")
              .append(m.getMetricsWindowEnd()).append("\n");
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportRulePerformanceAsJson() {
        List<RulePerformanceMetric> list = ruleRepository.findAll();
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String exportTrendsAsCsv() {
        List<InstitutionBehavioralTrend> list = trendRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,institutionId,totalVerifications,overrideRate,trendType,driftSeverity,fraudSignalsCount,averageConfidenceScore,lastDeviationDetectedAt,metricsWindowStart,metricsWindowEnd\n");
        
        for (InstitutionBehavioralTrend t : list) {
            sb.append(t.getId()).append(",")
              .append(t.getInstitutionId()).append(",")
              .append(t.getTotalVerifications()).append(",")
              .append(t.getOverrideRate()).append(",")
              .append(t.getTrendType()).append(",")
              .append(t.getDriftSeverity()).append(",")
              .append(t.getFraudSignalsCount()).append(",")
              .append(t.getAverageConfidenceScore()).append(",")
              .append(t.getLastDeviationDetectedAt() != null ? t.getLastDeviationDetectedAt().toString() : "").append(",")
              .append(t.getMetricsWindowStart()).append(",")
              .append(t.getMetricsWindowEnd()).append("\n");
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportTrendsAsJson() {
        List<InstitutionBehavioralTrend> list = trendRepository.findAll();
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
