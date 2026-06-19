package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation_escalations", uniqueConstraints = {
    @UniqueConstraint(name = "uq_rec_source", columnNames = {"recommendation_id", "source_entity_id", "source_type"})
}, indexes = {
    @Index(name = "idx_esc_rec", columnList = "recommendation_id"),
    @Index(name = "idx_esc_source", columnList = "source_entity_id, source_type")
})
public class RecommendationEscalation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recommendation_id", nullable = false)
    private UUID recommendationId;

    @Column(name = "source_entity_id", nullable = false)
    private UUID sourceEntityId;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "escalation_points", nullable = false)
    private Integer escalationPoints;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RecommendationEscalation() {}

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

    public UUID getSourceEntityId() {
        return sourceEntityId;
    }

    public void setSourceEntityId(UUID sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getEscalationPoints() {
        return escalationPoints;
    }

    public void setEscalationPoints(Integer escalationPoints) {
        this.escalationPoints = escalationPoints;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
