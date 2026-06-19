package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

public class CalibrationProposalDTO {
    private UUID id;
    private String targetParameter;
    private String parameterType;
    private String previousValue;
    private String proposedValue;
    private String justification;
    private String status;
    private String proposalConfidence;
    private Integer sampleSize;
    private Double overrideRate;
    private Double precisionRate;
    private Integer calibrationCount;
    private Instant lastCalibrationAt;
    private Instant createdAt;
    private String decisionByUsername;
    private Instant decisionAt;
    private String rejectionReason;

    public CalibrationProposalDTO() {}

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getDecisionByUsername() {
        return decisionByUsername;
    }

    public void setDecisionByUsername(String decisionByUsername) {
        this.decisionByUsername = decisionByUsername;
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
