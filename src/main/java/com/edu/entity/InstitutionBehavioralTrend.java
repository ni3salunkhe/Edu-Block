package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "institution_behavioral_trends", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"institution_id", "metrics_window_end"})
})
public class InstitutionBehavioralTrend {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(name = "total_verifications", nullable = false)
    private Long totalVerifications;

    @Column(name = "override_rate", nullable = false)
    private Double overrideRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend_type", nullable = false, length = 30)
    private TrendType trendType; // STABLE, DEVIATING, IMPROVING, VOLATILE

    @Enumerated(EnumType.STRING)
    @Column(name = "drift_severity", nullable = false, length = 20)
    private DriftSeverity driftSeverity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "fraud_signals_count", nullable = false)
    private Long fraudSignalsCount;

    @Column(name = "average_confidence_score", nullable = false)
    private Double averageConfidenceScore;

    @Column(name = "last_deviation_detected_at")
    private Instant lastDeviationDetectedAt;

    @Column(name = "metrics_window_start", nullable = false)
    private Instant metricsWindowStart;

    @Column(name = "metrics_window_end", nullable = false)
    private Instant metricsWindowEnd;

    public enum TrendType {
        STABLE,
        DEVIATING,
        IMPROVING,
        VOLATILE
    }

    public InstitutionBehavioralTrend() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
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

    public TrendType getTrendType() {
        return trendType;
    }

    public void setTrendType(TrendType trendType) {
        this.trendType = trendType;
    }

    public DriftSeverity getDriftSeverity() {
        return driftSeverity;
    }

    public void setDriftSeverity(DriftSeverity driftSeverity) {
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

    public Instant getMetricsWindowStart() {
        return metricsWindowStart;
    }

    public void setMetricsWindowStart(Instant metricsWindowStart) {
        this.metricsWindowStart = metricsWindowStart;
    }

    public Instant getMetricsWindowEnd() {
        return metricsWindowEnd;
    }

    public void setMetricsWindowEnd(Instant metricsWindowEnd) {
        this.metricsWindowEnd = metricsWindowEnd;
    }
}
