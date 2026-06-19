package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calibration_proposals", indexes = {
    @Index(name = "idx_calib_status_param", columnList = "status, target_parameter")
})
public class CalibrationProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "target_parameter", nullable = false, length = 100)
    private String targetParameter; // e.g., "AccreditationRule" or "APPROVE_threshold"

    @Column(name = "parameter_type", nullable = false, length = 30)
    private String parameterType; // RULE_MODIFIER, RECOMMENDATION_THRESHOLD

    @Column(name = "previous_value", nullable = false, length = 100)
    private String previousValue;

    @Column(name = "proposed_value", nullable = false, length = 100)
    private String proposedValue;

    @Column(name = "justification", nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status; // PENDING, APPROVED, REJECTED, SUPERSEDED

    @Column(name = "proposal_confidence", nullable = false, length = 20)
    private String proposalConfidence; // LOW, MEDIUM, HIGH

    @Column(name = "sample_size", nullable = false)
    private Integer sampleSize;

    @Column(name = "override_rate", nullable = false)
    private Double overrideRate;

    @Column(name = "precision_rate", nullable = false)
    private Double precisionRate;

    @Column(name = "calibration_count", nullable = false)
    private Integer calibrationCount = 0;

    @Column(name = "last_calibration_at")
    private Instant lastCalibrationAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decision_by")
    private UUID decisionBy; // Administrator User ID

    @Column(name = "decision_at")
    private Instant decisionAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    public enum ProposalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        SUPERSEDED
    }

    public CalibrationProposal() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTargetParameter() {
        return targetParameter;
    }

    public void setTargetParameter(String targetParameter) {
        this.targetParameter = targetParameter;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }

    public String getProposedValue() {
        return proposedValue;
    }

    public void setProposedValue(String proposedValue) {
        this.proposedValue = proposedValue;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public String getProposalConfidence() {
        return proposalConfidence;
    }

    public void setProposalConfidence(String proposalConfidence) {
        this.proposalConfidence = proposalConfidence;
    }

    public Integer getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(Integer sampleSize) {
        this.sampleSize = sampleSize;
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

    public Integer getCalibrationCount() {
        return calibrationCount;
    }

    public void setCalibrationCount(Integer calibrationCount) {
        this.calibrationCount = calibrationCount;
    }

    public Instant getLastCalibrationAt() {
        return lastCalibrationAt;
    }

    public void setLastCalibrationAt(Instant lastCalibrationAt) {
        this.lastCalibrationAt = lastCalibrationAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getDecisionBy() {
        return decisionBy;
    }

    public void setDecisionBy(UUID decisionBy) {
        this.decisionBy = decisionBy;
    }

    public Instant getDecisionAt() {
        return decisionAt;
    }

    public void setDecisionAt(Instant decisionAt) {
        this.decisionAt = decisionAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
