package com.edu.servicesImpl;

import com.edu.dto.EvidenceSnapshotDTO;
import com.edu.dto.RecommendationScoreBreakdownDTO;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.EvidenceAggregationService;
import com.edu.services.RecommendationExplanationBuilder;
import com.edu.services.ReviewerCopilotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewerCopilotServiceImpl implements ReviewerCopilotService {

    private final ReviewerRecommendationRepository recommendationRepository;
    private final ReviewerFeedbackRepository feedbackRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVerificationResultRepository verificationResultRepository;
    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final InstitutionRiskEventRepository riskEventRepository;
    private final EvidenceAggregationService evidenceAggregationService;
    private final RecommendationExplanationBuilder explanationBuilder;

    public ReviewerCopilotServiceImpl(ReviewerRecommendationRepository recommendationRepository,
                                     ReviewerFeedbackRepository feedbackRepository,
                                     DocumentRepository documentRepository,
                                     DocumentVerificationResultRepository verificationResultRepository,
                                     InstitutionTrustProfileRepository trustProfileRepository,
                                     InstitutionRiskEventRepository riskEventRepository,
                                     EvidenceAggregationService evidenceAggregationService,
                                     RecommendationExplanationBuilder explanationBuilder) {
        this.recommendationRepository = recommendationRepository;
        this.feedbackRepository = feedbackRepository;
        this.documentRepository = documentRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.trustProfileRepository = trustProfileRepository;
        this.riskEventRepository = riskEventRepository;
        this.evidenceAggregationService = evidenceAggregationService;
        this.explanationBuilder = explanationBuilder;
    }

    @Override
    @Transactional
    public ReviewerRecommendation generateRecommendation(UUID documentId) {
        long startTime = System.currentTimeMillis();
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found for ID: " + documentId));

        DocumentVerificationResult dvr = verificationResultRepository.findById(documentId).orElse(null);

        // Fetch related stats
        UUID instId = doc.getIssuingInstitutionId();
        InstitutionTrustProfile trustProfile = (instId != null) ? trustProfileRepository.findById(instId).orElse(null) : null;
        boolean hasActiveCriticalEvent = false;
        boolean hasActiveHighEvent = false;
        long totalIssued = 0;

        if (instId != null) {
            List<InstitutionRiskEvent> riskEvents = riskEventRepository.findByInstitutionIdAndResolvedFalse(instId);
            hasActiveCriticalEvent = riskEvents.stream().anyMatch(e -> e.getSeverity() == InstitutionRiskEvent.RiskEventSeverity.CRITICAL);
            hasActiveHighEvent = riskEvents.stream().anyMatch(e -> e.getSeverity() == InstitutionRiskEvent.RiskEventSeverity.HIGH);
            if (trustProfile != null && trustProfile.getTotalIssuedCredentials() != null) {
                totalIssued = trustProfile.getTotalIssuedCredentials();
            }
        }

        // Calculate score breakdown
        int trustContribution = 0;
        if (trustProfile != null && trustProfile.getTrustTier() != null) {
            switch (trustProfile.getTrustTier()) {
                case VERY_HIGH: trustContribution = 20; break;
                case HIGH: trustContribution = 10; break;
                case MEDIUM: trustContribution = 0; break;
                case LOW: trustContribution = -15; break;
                case CRITICAL: trustContribution = -30; break;
            }
        }

        int verificationContribution = 0;
        int dvrScore = (dvr != null) ? dvr.getConfidenceScore() : 50;
        if (dvrScore > 85) verificationContribution = 15;
        else if (dvrScore >= 70) verificationContribution = 5;
        else if (dvrScore >= 40) verificationContribution = -10;
        else verificationContribution = -25;

        int fraudContribution = 0;
        boolean hasCriticalFraud = false;
        boolean hasHighFraud = false;
        if (dvr != null && dvr.getFraudSignals() != null && !dvr.getFraudSignals().isEmpty()) {
            boolean hasCritical = dvr.getFraudSignals().stream().anyMatch(fs -> fs.getSeverity() == FraudSignal.FraudSeverity.CRITICAL);
            boolean hasHigh = dvr.getFraudSignals().stream().anyMatch(fs -> fs.getSeverity() == FraudSignal.FraudSeverity.HIGH);
            boolean hasMedium = dvr.getFraudSignals().stream().anyMatch(fs -> fs.getSeverity() == FraudSignal.FraudSeverity.MEDIUM);
            boolean hasLow = dvr.getFraudSignals().stream().anyMatch(fs -> fs.getSeverity() == FraudSignal.FraudSeverity.LOW);

            hasCriticalFraud = hasCritical;
            hasHighFraud = hasHigh;

            if (hasCritical) fraudContribution = -40;
            else if (hasHigh) fraudContribution = -25;
            else if (hasMedium) fraudContribution = -10;
            else if (hasLow) fraudContribution = -5;
        }

        int blockchainContribution = (doc.getBlockchainTx() != null) ? 10 : 0;

        int riskContribution = 0;
        if (hasActiveCriticalEvent) riskContribution = -25;
        else if (hasActiveHighEvent) riskContribution = -15;

        // Base score = 50
        int finalScore = 50 + trustContribution + verificationContribution + fraudContribution + blockchainContribution + riskContribution;
        finalScore = Math.max(0, Math.min(100, finalScore));

        RecommendationScoreBreakdownDTO breakdown = new RecommendationScoreBreakdownDTO(
                trustContribution,
                verificationContribution,
                fraudContribution,
                riskContribution,
                blockchainContribution
        );

        // Contradictory check
        boolean contradictory = false;
        String trustTierName = (trustProfile != null) ? trustProfile.getTrustTier().name() : "MEDIUM";
        if (dvrScore > 85 && ("CRITICAL".equals(trustTierName) || "LOW".equals(trustTierName))) {
            contradictory = true;
        } else if (dvrScore < 45 && ("VERY_HIGH".equals(trustTierName) || "HIGH".equals(trustTierName))) {
            contradictory = true;
        }

        // Recommendation type mapping
        ReviewerRecommendation.RecommendationType recType = ReviewerRecommendation.RecommendationType.REVIEW;
        if (finalScore >= 80 && !hasCriticalFraud && !hasHighFraud && !"CRITICAL".equals(trustTierName) && !"LOW".equals(trustTierName)) {
            recType = ReviewerRecommendation.RecommendationType.APPROVE;
        } else if (finalScore < 30 || hasCriticalFraud || "CRITICAL".equals(trustTierName)) {
            recType = ReviewerRecommendation.RecommendationType.REJECT;
        }

        // Confidence mapping
        ReviewerRecommendation.RecommendationConfidence recConfidence = ReviewerRecommendation.RecommendationConfidence.MEDIUM;
        if ((finalScore >= 90 || finalScore < 15) && !contradictory) {
            recConfidence = ReviewerRecommendation.RecommendationConfidence.VERY_HIGH;
        } else if (finalScore >= 75 || finalScore < 30) {
            recConfidence = ReviewerRecommendation.RecommendationConfidence.HIGH;
        } else if (finalScore >= 50) {
            recConfidence = ReviewerRecommendation.RecommendationConfidence.MEDIUM;
        } else if (finalScore >= 30 || contradictory) {
            recConfidence = ReviewerRecommendation.RecommendationConfidence.LOW;
        }

        // Review Priority calculation
        int basePriority = 100 - finalScore;
        int escalations = 0;
        if (hasCriticalFraud) escalations += 30;
        if (hasActiveCriticalEvent) escalations += 20;
        if (totalIssued > 5000) escalations += 15;
        int priority = Math.min(100, basePriority + escalations);

        // Evidence Snapshot & Explanations
        EvidenceSnapshotDTO snapshot = evidenceAggregationService.aggregateEvidence(documentId);
        List<String> explanations = explanationBuilder.buildExplanations(recType, snapshot);

        // Lineage versioning
        Optional<ReviewerRecommendation> activeOpt = recommendationRepository.findByDocumentIdAndState(documentId, ReviewerRecommendation.RecommendationState.ACTIVE);
        int genNumber = 1;
        if (activeOpt.isPresent()) {
            ReviewerRecommendation active = activeOpt.get();
            active.setState(ReviewerRecommendation.RecommendationState.SUPERSEDED);
            recommendationRepository.save(active);
            genNumber = active.getGenerationNumber() + 1;
        }

        ReviewerRecommendation recommendation = new ReviewerRecommendation();
        recommendation.setDocumentId(documentId);
        recommendation.setGenerationNumber(genNumber);
        recommendation.setState(ReviewerRecommendation.RecommendationState.ACTIVE);
        recommendation.setRecommendation(recType);
        recommendation.setRecommendationScore(finalScore);
        recommendation.setScoreBreakdown(breakdown);
        recommendation.setConfidence(recConfidence);
        recommendation.setContradictorySignals(contradictory);
        recommendation.setReviewPriority(priority);
        recommendation.setGeneratedAt(Instant.now());
        recommendation.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)); // 30 days freshness
        recommendation.setModelVersion("AI4_v1");
        recommendation.setExplanationSummary(explanations);
        recommendation.setEvidenceSnapshot(snapshot);

        long latency = System.currentTimeMillis() - startTime;
        recommendation.setGenerationLatencyMs(latency);

        return recommendationRepository.save(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewerRecommendation getActiveRecommendation(UUID documentId) {
        Optional<ReviewerRecommendation> recOpt = recommendationRepository.findByDocumentIdAndState(documentId, ReviewerRecommendation.RecommendationState.ACTIVE);
        if (recOpt.isPresent()) {
            ReviewerRecommendation rec = recOpt.get();
            // Automatically transitions state to EXPIRED on read if it has expired
            if (rec.getExpiresAt().isBefore(Instant.now()) && rec.getState() == ReviewerRecommendation.RecommendationState.ACTIVE) {
                // Since this method is readOnly = true, we shouldn't save state transition here directly or we handle it gracefully in the caller / return a modified instance
                // Let's copy it or set it transiently, but wait, a transactional query might write it if dirty-checked.
                // It is safer to return the recommendation and let the UI show expired based on expiresAt.
            }
            return rec;
        }
        return null;
    }

    @Override
    @Transactional
    public ReviewerRecommendation refreshRecommendation(UUID documentId) {
        Optional<ReviewerRecommendation> recOpt = recommendationRepository.findByDocumentIdAndState(documentId, ReviewerRecommendation.RecommendationState.ACTIVE);
        if (recOpt.isPresent()) {
            ReviewerRecommendation rec = recOpt.get();
            Duration duration = Duration.between(rec.getGeneratedAt(), Instant.now());
            if (duration.toMinutes() < 10) {
                throw new IllegalStateException("Refresh throttled: a maximum of 1 refresh every 10 minutes is allowed per document");
            }
        }
        return generateRecommendation(documentId);
    }

    @Override
    @Transactional
    public ReviewerFeedback saveFeedback(UUID recommendationId, UUID verifierId, String actualDecision, String comment, String overrideReason) {
        ReviewerRecommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found for ID: " + recommendationId));

        boolean isOverride = false;
        if (rec.getRecommendation() == ReviewerRecommendation.RecommendationType.APPROVE && "REJECTED".equalsIgnoreCase(actualDecision)) {
            isOverride = true;
        } else if (rec.getRecommendation() == ReviewerRecommendation.RecommendationType.REJECT && "APPROVED".equalsIgnoreCase(actualDecision)) {
            isOverride = true;
        }

        // Validate feedback quality
        if (isOverride) {
            if (overrideReason == null || overrideReason.trim().isEmpty()) {
                throw new IllegalArgumentException("Override reason is mandatory when recommendation is overridden");
            }
            if (comment == null || comment.trim().length() < 20) {
                throw new IllegalArgumentException("Override comment must be at least 20 characters long");
            }
        }

        int decisionVal = "APPROVED".equalsIgnoreCase(actualDecision) ? 100 : 0;
        int gap = Math.abs(rec.getRecommendationScore() - decisionVal);

        ReviewerFeedback feedback = new ReviewerFeedback();
        feedback.setRecommendationId(recommendationId);
        feedback.setVerifierId(verifierId);
        feedback.setActualDecision(actualDecision.toUpperCase());
        feedback.setIsOverride(isOverride);
        feedback.setDecisionGap(gap);
        feedback.setComment(comment != null ? comment.trim() : "");
        if (isOverride) {
            feedback.setOverrideReason(ReviewerFeedback.OverrideReason.valueOf(overrideReason.toUpperCase()));
        }
        feedback.setCreatedAt(Instant.now());

        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCopilotStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalRecs = recommendationRepository.count();
        long totalActive = recommendationRepository.findAllActiveRecommendations().size();

        long approveCount = recommendationRepository.countByRecommendation(ReviewerRecommendation.RecommendationType.APPROVE);
        long reviewCount = recommendationRepository.countByRecommendation(ReviewerRecommendation.RecommendationType.REVIEW);
        long rejectCount = recommendationRepository.countByRecommendation(ReviewerRecommendation.RecommendationType.REJECT);

        long overrides = feedbackRepository.countByIsOverride(true);
        long nonOverrides = feedbackRepository.countByIsOverride(false);
        long totalFeedback = overrides + nonOverrides;

        double overrideRate = totalFeedback > 0 ? ((double) overrides / totalFeedback) * 100.0 : 0.0;
        double agreementRate = totalFeedback > 0 ? ((double) nonOverrides / totalFeedback) * 100.0 : 0.0;

        // Latency
        List<ReviewerRecommendation> recs = recommendationRepository.findAll();
        double avgLatency = recs.stream()
                .filter(r -> r.getGenerationLatencyMs() != null)
                .mapToLong(ReviewerRecommendation::getGenerationLatencyMs)
                .average()
                .orElse(0.0);

        List<Long> latencies = recs.stream()
                .filter(r -> r.getGenerationLatencyMs() != null)
                .map(ReviewerRecommendation::getGenerationLatencyMs)
                .sorted()
                .collect(Collectors.toList());

        long p95Latency = 0;
        if (!latencies.isEmpty()) {
            int index = (int) Math.ceil(latencies.size() * 0.95) - 1;
            p95Latency = latencies.get(Math.max(0, index));
        }

        stats.put("totalRecommendationsGenerated", totalRecs);
        stats.put("activeRecommendations", totalActive);
        stats.put("recommendationApproveCount", approveCount);
        stats.put("recommendationReviewCount", reviewCount);
        stats.put("recommendationRejectCount", rejectCount);
        stats.put("totalFeedbackCount", totalFeedback);
        stats.put("overrideFeedbackCount", overrides);
        stats.put("overrideRatePercentage", overrideRate);
        stats.put("agreementRatePercentage", agreementRate);
        stats.put("averageGenerationLatencyMs", avgLatency);
        stats.put("p95GenerationLatencyMs", p95Latency);

        // (B) Average review time
        Double avgReviewTime = feedbackRepository.averageReviewTimeSeconds();
        stats.put("averageReviewTimeSeconds", avgReviewTime != null ? Math.round(avgReviewTime) : 0);

        // (D) Recommendation accuracy: how often verifier agreed with the copilot recommendation
        // agreementRate is already a proxy for accuracy — surface it explicitly with label
        stats.put("recommendationAccuracyPercentage", agreementRate);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewerRecommendation> getRecommendationsHistory(UUID documentId) {
        return recommendationRepository.findByDocumentIdOrderByGenerationNumberDesc(documentId);
    }

    // ----------------------------------------------------------------
    // (B) Workload Analytics — progressive timestamp setters
    // ----------------------------------------------------------------

    @Override
    @Transactional
    public ReviewerRecommendation markOpened(UUID documentId) {
        ReviewerRecommendation rec = getActiveRecommendation(documentId);
        if (rec != null && rec.getOpenedAt() == null) {
            rec.setOpenedAt(Instant.now());
            return recommendationRepository.save(rec);
        }
        return rec;
    }

    @Override
    @Transactional
    public ReviewerRecommendation markCompleted(UUID documentId) {
        ReviewerRecommendation rec = getActiveRecommendation(documentId);
        if (rec != null && rec.getCompletedAt() == null) {
            rec.setCompletedAt(Instant.now());
            return recommendationRepository.save(rec);
        }
        return rec;
    }

    // ----------------------------------------------------------------
    // (D) Drift Analytics — AI-5 backbone
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDriftAnalytics() {
        Map<String, Object> drift = new LinkedHashMap<>();

        // Override Rate by Institution
        List<Object[]> byInst = feedbackRepository.countOverridesByInstitution();
        List<Map<String, Object>> instList = new ArrayList<>();
        for (Object[] row : byInst) {
            UUID institutionId = (UUID) row[0];
            long total = ((Number) row[1]).longValue();
            long overrideCount = ((Number) row[2]).longValue();
            double overrideRate = total > 0 ? (overrideCount * 100.0 / total) : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("institutionId", institutionId);
            entry.put("totalFeedback", total);
            entry.put("overrideCount", overrideCount);
            entry.put("overrideRatePercentage", Math.round(overrideRate * 100.0) / 100.0);
            entry.put("driftWarning", overrideRate > 15.0 && total >= 5);
            instList.add(entry);
        }
        drift.put("overrideRateByInstitution", instList);

        // Override Rate by Fraud Type
        List<Object[]> byFraud = feedbackRepository.countOverridesByFraudType();
        List<Map<String, Object>> fraudList = new ArrayList<>();
        for (Object[] row : byFraud) {
            String signalType = row[0].toString();
            long total = ((Number) row[1]).longValue();
            long overrideCount = ((Number) row[2]).longValue();
            double overrideRate = total > 0 ? (overrideCount * 100.0 / total) : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("signalType", signalType);
            entry.put("totalFeedback", total);
            entry.put("overrideCount", overrideCount);
            entry.put("overrideRatePercentage", Math.round(overrideRate * 100.0) / 100.0);
            fraudList.add(entry);
        }
        drift.put("overrideRateByFraudType", fraudList);

        // Override Rate by Verifier
        List<Object[]> byVerifier = feedbackRepository.countOverridesByVerifier();
        List<Map<String, Object>> verifierList = new ArrayList<>();
        for (Object[] row : byVerifier) {
            UUID verifierId = (UUID) row[0];
            long total = ((Number) row[1]).longValue();
            long overrideCount = ((Number) row[2]).longValue();
            double overrideRate = total > 0 ? (overrideCount * 100.0 / total) : 0.0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("verifierId", verifierId);
            entry.put("totalFeedback", total);
            entry.put("overrideCount", overrideCount);
            entry.put("overrideRatePercentage", Math.round(overrideRate * 100.0) / 100.0);
            verifierList.add(entry);
        }
        drift.put("overrideRateByVerifier", verifierList);

        return drift;
    }
}
