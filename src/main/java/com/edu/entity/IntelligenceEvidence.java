package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intelligence_evidence", indexes = {
    @Index(name = "idx_evidence_source", columnList = "source_id, source_type")
})
public class IntelligenceEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "evidence_code", nullable = false, length = 50)
    private String evidenceCode;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "metric_value", nullable = false, length = 100)
    private String metricValue;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public IntelligenceEvidence() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
