package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviewer_feedback", indexes = {
    @Index(name = "idx_feedback_rec_id", columnList = "recommendation_id")
})
public class ReviewerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recommendation_id", nullable = false, unique = true)
    private UUID recommendationId;

    @Column(name = "verifier_id", nullable = false)
    private UUID verifierId;

    @Column(name = "actual_decision", nullable = false, length = 20)
    private String actualDecision; // "APPROVED" or "REJECTED"

    @Column(nullable = false)
    private Boolean isOverride;

    @Column(nullable = false)
    private Integer decisionGap; // Math.abs(recScore - (actualDecisionVal))

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment; // Strictly mandatory on override, validated to >= 20 characters

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private OverrideReason overrideReason; // Strictly mandatory on override

    @Column(nullable = false)
    private Instant createdAt;

    public enum OverrideReason {
        FALSE_POSITIVE_FRAUD,
        TRUST_PROFILE_OUTDATED,
        ALTERNATIVE_VALID_FORMAT,
        LEGITIMATE_STUDENT_MATCH,
        OTHER
    }

    public ReviewerFeedback() {}

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

    public UUID getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(UUID verifierId) {
        this.verifierId = verifierId;
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

    public Integer getDecisionGap() {
        return decisionGap;
    }

    public void setDecisionGap(Integer decisionGap) {
        this.decisionGap = decisionGap;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public OverrideReason getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(OverrideReason overrideReason) {
        this.overrideReason = overrideReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
