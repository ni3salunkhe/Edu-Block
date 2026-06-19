package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

public class TrendAlertDTO {
    private UUID institutionId;
    private String institutionName;
    private Long totalVerifications;
    private Double overrideRate;
    private String trendType;
    private String driftSeverity;
    private Long fraudSignalsCount;
    private Double averageConfidenceScore;
    private Instant lastDeviationDetectedAt;

    public TrendAlertDTO() {}

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public Long getTotalVerifications() {
        return totalVerifications;
    }

    public void setTotalVerifications(Long totalVerifications) {
        this.totalVerifications = totalVerifications;
    }

    public Double getOverrideRate() {
        return overrideRate;
    }

    public void setOverrideRate(Double overrideRate) {
        this.overrideRate = overrideRate;
    }

    public String getTrendType() {
        return trendType;
    }

    public void setTrendType(String trendType) {
        this.trendType = trendType;
    }

    public String getDriftSeverity() {
        return driftSeverity;
    }

    public void setDriftSeverity(String driftSeverity) {
        this.driftSeverity = driftSeverity;
    }

    public Long getFraudSignalsCount() {
        return fraudSignalsCount;
    }

    public void setFraudSignalsCount(Long fraudSignalsCount) {
        this.fraudSignalsCount = fraudSignalsCount;
    }

    public Double getAverageConfidenceScore() {
        return averageConfidenceScore;
    }

    public void setAverageConfidenceScore(Double averageConfidenceScore) {
        this.averageConfidenceScore = averageConfidenceScore;
    }

    public Instant getLastDeviationDetectedAt() {
        return lastDeviationDetectedAt;
    }

    public void setLastDeviationDetectedAt(Instant lastDeviationDetectedAt) {
        this.lastDeviationDetectedAt = lastDeviationDetectedAt;
    }
}
