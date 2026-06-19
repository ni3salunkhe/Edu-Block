package com.edu.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class LearningSignalDTO {
    private UUID id;
    private UUID recommendationId;
    private UUID documentId;
    private UUID institutionId;
    private UUID verifierId;
    private String recommendedAction;
    private String actualDecision;
    private Boolean isOverride;
    private String overrideReason;
    private Integer decisionGap;
    private Long reviewTimeSeconds;
    private List<String> triggeredRuleCodes;
    private List<String> triggeredFraudSignalCodes;
    private Integer verificationConfidenceScore;
    private String riskLevel;
    private String confidenceLevel;
    private String verificationEngineVersion;
    private String trustEngineVersion;
    private String copilotVersion;
    private String ruleConfigurationVersion;
    private Instant createdAt;

    public LearningSignalDTO() {}

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
