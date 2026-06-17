package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "document_verification_results", indexes = {
    @Index(name = "idx_doc_verify_score", columnList = "confidenceScore"),
    @Index(name = "idx_doc_verify_level", columnList = "confidenceLevel"),
    @Index(name = "idx_doc_verify_risk", columnList = "riskLevel"),
    @Index(name = "idx_doc_verify_version", columnList = "verificationModelVersion")
})
public class DocumentVerificationResult {

    @Id
    private UUID documentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(nullable = false)
    private Integer confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConfidenceLevel confidenceLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(nullable = false)
    private Boolean isVerified;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanationsJson;

    @Column(nullable = false, length = 64)
    private String ocrTextHash;

    @Column(nullable = false, length = 20)
    private String verificationModelVersion;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "document_id")
    private List<FraudSignal> fraudSignals;

    @Column(nullable = false)
    private Instant processedAt;

    public enum ConfidenceLevel {
        VERY_HIGH,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum RiskLevel {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public DocumentVerificationResult() {}

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Integer getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Integer confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean verified) {
        isVerified = verified;
    }

    public String getExplanationsJson() {
        return explanationsJson;
    }

    public void setExplanationsJson(String explanationsJson) {
        this.explanationsJson = explanationsJson;
    }

    public String getOcrTextHash() {
        return ocrTextHash;
    }

    public void setOcrTextHash(String ocrTextHash) {
        this.ocrTextHash = ocrTextHash;
    }

    public String getVerificationModelVersion() {
        return verificationModelVersion;
    }

    public void setVerificationModelVersion(String verificationModelVersion) {
        this.verificationModelVersion = verificationModelVersion;
    }

    public List<FraudSignal> getFraudSignals() {
        return fraudSignals;
    }

    public void setFraudSignals(List<FraudSignal> fraudSignals) {
        this.fraudSignals = fraudSignals;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
