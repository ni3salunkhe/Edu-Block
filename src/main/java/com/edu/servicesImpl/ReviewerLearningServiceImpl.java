package com.edu.servicesImpl;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.ReviewerLearningService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewerLearningServiceImpl implements ReviewerLearningService {

    private final ReviewerFeedbackRepository feedbackRepository;
    private final ReviewerRecommendationRepository recommendationRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVerificationResultRepository verificationResultRepository;
    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final ReviewerLearningSignalRepository learningSignalRepository;
    private final ObjectMapper objectMapper;

    public ReviewerLearningServiceImpl(ReviewerFeedbackRepository feedbackRepository,
                                       ReviewerRecommendationRepository recommendationRepository,
                                       DocumentRepository documentRepository,
                                       DocumentVerificationResultRepository verificationResultRepository,
                                       InstitutionTrustProfileRepository trustProfileRepository,
                                       ReviewerLearningSignalRepository learningSignalRepository,
                                       ObjectMapper objectMapper) {
        this.feedbackRepository = feedbackRepository;
        this.recommendationRepository = recommendationRepository;
        this.documentRepository = documentRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.trustProfileRepository = trustProfileRepository;
        this.learningSignalRepository = learningSignalRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ReviewerLearningSignal processFeedbackSignal(UUID feedbackId) {
        ReviewerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found for ID: " + feedbackId));

        ReviewerRecommendation rec = recommendationRepository.findById(feedback.getRecommendationId())
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found for ID: " + feedback.getRecommendationId()));

        Document doc = documentRepository.findById(rec.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found for ID: " + rec.getDocumentId()));

        DocumentVerificationResult dvr = verificationResultRepository.findById(doc.getId()).orElse(null);

        // Map triggered rules from verification results JSON
        List<String> triggeredRules = new ArrayList<>();
        if (dvr != null && dvr.getExplanationsJson() != null && !dvr.getExplanationsJson().trim().isEmpty()) {
            try {
                List<Map<String, Object>> explanations = objectMapper.readValue(
                        dvr.getExplanationsJson(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                for (Map<String, Object> exp : explanations) {
                    Boolean triggered = (Boolean) exp.get("triggered");
                    String ruleName = (String) exp.get("ruleName");
                    if (Boolean.TRUE.equals(triggered) && ruleName != null) {
                        triggeredRules.add(ruleName);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Map triggered fraud signals
        List<String> triggeredFrauds = new ArrayList<>();
        if (dvr != null && dvr.getFraudSignals() != null) {
            triggeredFrauds = dvr.getFraudSignals().stream()
                    .map(fs -> fs.getSignalType().name())
                    .collect(Collectors.toList());
        }

        // Calculate review duration
        long duration = 0;
        if (rec.getOpenedAt() != null && rec.getCompletedAt() != null) {
            duration = Duration.between(rec.getOpenedAt(), rec.getCompletedAt()).toSeconds();
        }

        // Fetch engine lineage configurations
        String verificationVersion = (dvr != null) ? dvr.getVerificationModelVersion() : "AI2_v1";
        String trustVersion = "AI3_v1";
        if (doc.getIssuingInstitutionId() != null) {
            Optional<InstitutionTrustProfile> tpOpt = trustProfileRepository.findById(doc.getIssuingInstitutionId());
            if (tpOpt.isPresent()) {
                trustVersion = tpOpt.get().getCalculationVersion();
            }
        }
        String copilotVersion = rec.getModelVersion() != null ? rec.getModelVersion() : "AI4_v1";
        String rulesetVersion = "RULESET_2026_06"; // Static code ruleset mapping

        ReviewerLearningSignal signal = new ReviewerLearningSignal();
        signal.setRecommendationId(rec.getId());
        signal.setDocumentId(doc.getId());
        signal.setInstitutionId(doc.getIssuingInstitutionId());
        signal.setVerifierId(feedback.getVerifierId());
        signal.setRecommendedAction(rec.getRecommendation().name());
        signal.setActualDecision(feedback.getActualDecision());
        signal.setIsOverride(feedback.getIsOverride());
        signal.setOverrideReason(feedback.getOverrideReason() != null ? feedback.getOverrideReason().name() : null);
        signal.setDecisionGap(feedback.getDecisionGap());
        signal.setReviewTimeSeconds(duration);
        signal.setTriggeredRuleCodes(triggeredRules);
        signal.setTriggeredFraudSignalCodes(triggeredFrauds);
        
        // Preservation values from AI-2
        if (dvr != null) {
            signal.setVerificationConfidenceScore(dvr.getConfidenceScore());
            signal.setRiskLevel(dvr.getRiskLevel().name());
            signal.setConfidenceLevel(dvr.getConfidenceLevel().name());
        } else {
            signal.setVerificationConfidenceScore(50);
            signal.setRiskLevel("MEDIUM");
            signal.setConfidenceLevel("MEDIUM");
        }

        signal.setVerificationEngineVersion(verificationVersion);
        signal.setTrustEngineVersion(trustVersion);
        signal.setCopilotVersion(copilotVersion);
        signal.setRuleConfigurationVersion(rulesetVersion);
        signal.setCreatedAt(Instant.now());

        return learningSignalRepository.save(signal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerLearningSignal> getRecentSignals() {
        return learningSignalRepository.findAll();
    }
}
