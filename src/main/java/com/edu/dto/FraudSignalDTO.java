package com.edu.dto;

import com.edu.entity.FraudSignal.FraudSignalType;
import com.edu.entity.FraudSignal.FraudSeverity;
import java.time.Instant;
import java.util.UUID;

public class FraudSignalDTO {

    private UUID id;
    private FraudSignalType signalType;
    private FraudSeverity severity;
    private String description;
    private String evidenceJson;
    private Instant detectedAt;

    public FraudSignalDTO() {}

    public FraudSignalDTO(UUID id, FraudSignalType signalType, FraudSeverity severity, String description, String evidenceJson, Instant detectedAt) {
        this.id = id;
        this.signalType = signalType;
        this.severity = severity;
        this.description = description;
        this.evidenceJson = evidenceJson;
        this.detectedAt = detectedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FraudSignalType getSignalType() {
        return signalType;
    }

    public void setSignalType(FraudSignalType signalType) {
        this.signalType = signalType;
    }

    public FraudSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(FraudSeverity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceJson() {
        return evidenceJson;
    }

    public void setEvidenceJson(String evidenceJson) {
        this.evidenceJson = evidenceJson;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }
}
