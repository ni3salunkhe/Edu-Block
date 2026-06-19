package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rule_performance_metrics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"rule_name", "target_type", "metrics_window_end"})
})
public class RulePerformanceMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType; // RULE, FRAUD_SIGNAL

    @Column(name = "total_evaluations", nullable = false)
    private Long totalEvaluations;

    @Column(name = "override_count", nullable = false)
    private Long overrideCount;

    @Column(name = "true_positives", nullable = false)
    private Long truePositives;

    @Column(name = "false_positives", nullable = false)
    private Long falsePositives;

    @Column(name = "false_negatives", nullable = false)
    private Long falseNegatives;

    @Column(name = "precision_rate", nullable = false)
    private Double precisionRate;

    @Column(name = "override_rate", nullable = false)
    private Double overrideRate;

    @Column(name = "metrics_window_start", nullable = false)
    private Instant metricsWindowStart;

    @Column(name = "metrics_window_end", nullable = false)
    private Instant metricsWindowEnd;

    public RulePerformanceMetric() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTotalEvaluations() {
        return totalEvaluations;
    }

    public void setTotalEvaluations(Long totalEvaluations) {
        this.totalEvaluations = totalEvaluations;
    }

    public Long getOverrideCount() {
        return overrideCount;
    }

    public void setOverrideCount(Long overrideCount) {
        this.overrideCount = overrideCount;
    }

    public Long getTruePositives() {
        return truePositives;
    }

    public void setTruePositives(Long truePositives) {
        this.truePositives = truePositives;
    }

    public Long getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(Long falsePositives) {
        this.falsePositives = falsePositives;
    }

    public Long getFalseNegatives() {
        return falseNegatives;
    }

    public void setFalseNegatives(Long falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public Double getPrecisionRate() {
        return precisionRate;
    }

    public void setPrecisionRate(Double precisionRate) {
        this.precisionRate = precisionRate;
    }

    public Double getOverrideRate() {
        return overrideRate;
    }

    public void setOverrideRate(Double overrideRate) {
        this.overrideRate = overrideRate;
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
