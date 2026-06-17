package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_signals", indexes = {
    @Index(name = "idx_fraud_doc_id", columnList = "document_id"),
    @Index(name = "idx_fraud_type", columnList = "signalType")
})
public class FraudSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FraudSignalType signalType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FraudSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String evidenceJson;

    @Column(nullable = false)
    private Instant detectedAt;

    public enum FraudSignalType {
        DUPLICATE_CERTIFICATE,
        SAME_CREDENTIAL_REFERENCE_MULTIPLE_TIMES,
        INSTITUTION_MISMATCH,
        DEGREE_MISMATCH,
        SUSPICIOUS_OCR_PATTERN,
        ALTERED_IMAGE_INDICATOR,
        TEMPLATE_MANIPULATION,
        DOCUMENT_TYPE_MISMATCH,
        FUTURE_DATE_DETECTED,
        MULTIPLE_NAMES_DETECTED,
        LOW_TEXT_DENSITY
    }

    public enum FraudSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public FraudSignal() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
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
