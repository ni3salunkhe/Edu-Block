package com.edu.dto;

import java.util.UUID;

public class VerifierQualityDTO {
    private UUID verifierId;
    private String verifierUsername;
    private Long totalReviews;
    private Long overrideCount;
    private Double overrideRate;
    private Double averageReviewTimeSeconds;
    private Double consensusAgreementRate;
    private Double decisionVariance;
    private Boolean rubberStampingRisk;

    public VerifierQualityDTO() {}

    public UUID getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(UUID verifierId) {
        this.verifierId = verifierId;
    }

    public String getVerifierUsername() {
        return verifierUsername;
    }

    public void setVerifierUsername(String verifierUsername) {
        this.verifierUsername = verifierUsername;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Long getOverrideCount() {
        return overrideCount;
    }

    public void setOverrideCount(Long overrideCount) {
        this.overrideCount = overrideCount;
    }

    public Double getOverrideRate() {
        return overrideRate;
    }

    public void setOverrideRate(Double overrideRate) {
        this.overrideRate = overrideRate;
    }

    public Double getAverageReviewTimeSeconds() {
        return averageReviewTimeSeconds;
    }

    public void setAverageReviewTimeSeconds(Double averageReviewTimeSeconds) {
        this.averageReviewTimeSeconds = averageReviewTimeSeconds;
    }

    public Double getConsensusAgreementRate() {
        return consensusAgreementRate;
    }

    public void setConsensusAgreementRate(Double consensusAgreementRate) {
        this.consensusAgreementRate = consensusAgreementRate;
    }

    public Double getDecisionVariance() {
        return decisionVariance;
    }

    public void setDecisionVariance(Double decisionVariance) {
        this.decisionVariance = decisionVariance;
    }

    public Boolean getRubberStampingRisk() {
        return rubberStampingRisk;
    }

    public void setRubberStampingRisk(Boolean rubberStampingRisk) {
        this.rubberStampingRisk = rubberStampingRisk;
    }
}
