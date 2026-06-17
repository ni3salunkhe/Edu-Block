package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "institution_risk_events", indexes = {
    @Index(name = "idx_inst_risk_resolved", columnList = "institution_id, resolved")
})
public class InstitutionRiskEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RiskEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskEventSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String evidenceJson;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    private Boolean resolved;

    public enum RiskEventType {
        FRAUD_SPIKE,
        ACCREDITATION_REVOKED,
        DUPLICATE_CREDENTIAL_CLUSTER,
        MASS_VERIFICATION_FAILURE
    }

    public enum RiskEventSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public InstitutionRiskEvent() {}

    public InstitutionRiskEvent(UUID institutionId, RiskEventType eventType, RiskEventSeverity severity, String description, String evidenceJson, Instant timestamp, Instant expiresAt, Boolean resolved) {
        this.institutionId = institutionId;
        this.eventType = eventType;
        this.severity = severity;
        this.description = description;
        this.evidenceJson = evidenceJson;
        this.timestamp = timestamp;
        this.expiresAt = expiresAt;
        this.resolved = resolved;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public RiskEventType getEventType() { return eventType; }
    public void setEventType(RiskEventType eventType) { this.eventType = eventType; }

    public RiskEventSeverity getSeverity() { return severity; }
    public void setSeverity(RiskEventSeverity severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }
}
