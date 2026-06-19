package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation_performance_metrics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"recommendation_type", "metrics_window_end"})
})
public class RecommendationPerformanceMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recommendation_type", nullable = false, length = 20)
    private String recommendationType; // APPROVE, REVIEW, REJECT

    @Column(name = "total_recommendations", nullable = false)
    private Long totalRecommendations;

    @Column(name = "accepted_recommendations", nullable = false)
    private Long acceptedRecommendations;

    @Column(name = "overridden_recommendations", nullable = false)
    private Long overriddenRecommendations;

    @Column(name = "acceptance_rate", nullable = false)
    private Double acceptanceRate;

    @Column(name = "override_rate", nullable = false)
    private Double overrideRate;

    @Column(name = "precision_rate", nullable = false)
    private Double precisionRate;

    @Column(name = "metrics_window_start", nullable = false)
    private Instant metricsWindowStart;

    @Column(name = "metrics_window_end", nullable = false)
    private Instant metricsWindowEnd;

    public RecommendationPerformanceMetric() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public Long getTotalRecommendations() {
        return totalRecommendations;
    }

    public void setTotalRecommendations(Long totalRecommendations) {
        this.totalRecommendations = totalRecommendations;
    }

    public Long getAcceptedRecommendations() {
        return acceptedRecommendations;
    }

    public void setAcceptedRecommendations(Long acceptedRecommendations) {
        this.acceptedRecommendations = acceptedRecommendations;
    }

    public Long getOverriddenRecommendations() {
        return overriddenRecommendations;
    }

    public void setOverriddenRecommendations(Long overriddenRecommendations) {
        this.overriddenRecommendations = overriddenRecommendations;
    }

    public Double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public Double getOverrideRate() {
        return overrideRate;
    }

    public void setOverrideRate(Double overrideRate) {
        this.overrideRate = overrideRate;
    }

    public Double getPrecisionRate() {
        return precisionRate;
    }

    public void setPrecisionRate(Double precisionRate) {
        this.precisionRate = precisionRate;
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
