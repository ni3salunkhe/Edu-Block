package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.edu.util.StringListConverter;

@Entity
@Table(name = "reviewer_learning_signals", indexes = {
    @Index(name = "idx_learn_inst_created", columnList = "institution_id, created_at"),
    @Index(name = "idx_learn_verifier_created", columnList = "verifier_id, created_at"),
    @Index(name = "idx_learn_override_created", columnList = "is_override, created_at")
})
public class ReviewerLearningSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recommendation_id", nullable = false)
    private UUID recommendationId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "institution_id")
    private UUID institutionId;

    @Column(name = "verifier_id", nullable = false)
    private UUID verifierId;

    @Column(name = "recommended_action", nullable = false, length = 20)
    private String recommendedAction; // APPROVE, REVIEW, REJECT

    @Column(name = "actual_decision", nullable = false, length = 20)
    private String actualDecision; // APPROVED, REJECTED

    @Column(name = "is_override", nullable = false)
    private Boolean isOverride;

    @Column(name = "override_reason", length = 50)
    private String overrideReason;

    @Column(name = "decision_gap", nullable = false)
    private Integer decisionGap;

    @Column(name = "review_time_seconds", nullable = false)
    private Long reviewTimeSeconds;

    @Convert(converter = StringListConverter.class)
    @Column(name = "triggered_rule_codes", nullable = false, columnDefinition = "TEXT")
    private List<String> triggeredRuleCodes;

    @Convert(converter = StringListConverter.class)
    @Column(name = "triggered_fraud_signal_codes", nullable = false, columnDefinition = "TEXT")
    private List<String> triggeredFraudSignalCodes;

    @Column(name = "verification_confidence_score")
    private Integer verificationConfidenceScore;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "confidence_level", length = 20)
    private String confidenceLevel;

    @Column(name = "verification_engine_version", length = 30)
    private String verificationEngineVersion;

    @Column(name = "trust_engine_version", length = 30)
    private String trustEngineVersion;

    @Column(name = "copilot_version", length = 30)
    private String copilotVersion;

    @Column(name = "rule_configuration_version", length = 50)
    private String ruleConfigurationVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ReviewerLearningSignal() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(UUID recommendationId) {
        this.recommendationId = recommendationId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public UUID getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(UUID verifierId) {
        this.verifierId = verifierId;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getActualDecision() {
        return actualDecision;
    }

    public void setActualDecision(String actualDecision) {
        this.actualDecision = actualDecision;
    }

    public Boolean getIsOverride() {
        return isOverride;
    }

    public void setIsOverride(Boolean isOverride) {
        this.isOverride = isOverride;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public Integer getDecisionGap() {
        return decisionGap;
    }

    public void setDecisionGap(Integer decisionGap) {
        this.decisionGap = decisionGap;
    }

    public Long getReviewTimeSeconds() {
        return reviewTimeSeconds;
    }

    public void setReviewTimeSeconds(Long reviewTimeSeconds) {
        this.reviewTimeSeconds = reviewTimeSeconds;
    }

    public List<String> getTriggeredRuleCodes() {
        return triggeredRuleCodes;
    }

    public void setTriggeredRuleCodes(List<String> triggeredRuleCodes) {
        this.triggeredRuleCodes = triggeredRuleCodes;
    }

    public List<String> getTriggeredFraudSignalCodes() {
        return triggeredFraudSignalCodes;
    }

    public void setTriggeredFraudSignalCodes(List<String> triggeredFraudSignalCodes) {
        this.triggeredFraudSignalCodes = triggeredFraudSignalCodes;
    }

    public Integer getVerificationConfidenceScore() {
        return verificationConfidenceScore;
    }

    public void setVerificationConfidenceScore(Integer verificationConfidenceScore) {
        this.verificationConfidenceScore = verificationConfidenceScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getVerificationEngineVersion() {
        return verificationEngineVersion;
    }

    public void setVerificationEngineVersion(String verificationEngineVersion) {
        this.verificationEngineVersion = verificationEngineVersion;
    }

    public String getTrustEngineVersion() {
        return trustEngineVersion;
    }

    public void setTrustEngineVersion(String trustEngineVersion) {
        this.trustEngineVersion = trustEngineVersion;
    }

    public String getCopilotVersion() {
        return copilotVersion;
    }

    public void setCopilotVersion(String copilotVersion) {
        this.copilotVersion = copilotVersion;
    }

    public String getRuleConfigurationVersion() {
        return ruleConfigurationVersion;
    }

    public void setRuleConfigurationVersion(String ruleConfigurationVersion) {
        this.ruleConfigurationVersion = ruleConfigurationVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
