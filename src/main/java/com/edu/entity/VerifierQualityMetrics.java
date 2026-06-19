package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verifier_quality_metrics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"verifier_id", "metrics_window_end"})
})
public class VerifierQualityMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "verifier_id", nullable = false)
    private UUID verifierId;

    @Column(name = "total_reviews", nullable = false)
    private Long totalReviews;

    @Column(name = "override_count", nullable = false)
    private Long overrideCount;

    @Column(name = "override_rate", nullable = false)
    private Double overrideRate;

    @Column(name = "average_review_time_seconds", nullable = false)
    private Double averageReviewTimeSeconds;

    @Column(name = "consensus_agreement_rate", nullable = false)
    private Double consensusAgreementRate;

    @Column(name = "decision_variance", nullable = false)
    private Double decisionVariance;

    @Column(name = "rubber_stamping_risk", nullable = false)
    private Boolean rubberStampingRisk;

    @Column(name = "metrics_window_start", nullable = false)
    private Instant metricsWindowStart;

    @Column(name = "metrics_window_end", nullable = false)
    private Instant metricsWindowEnd;

    public VerifierQualityMetrics() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(UUID verifierId) {
        this.verifierId = verifierId;
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
