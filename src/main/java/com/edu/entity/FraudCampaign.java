package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_campaigns", indexes = {
    @Index(name = "idx_camp_status", columnList = "status"),
    @Index(name = "idx_camp_score", columnList = "campaign_score")
})
public class FraudCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "template_identifier", length = 100)
    private String templateIdentifier;

    @Column(name = "institution_id")
    private UUID institutionId;

    @Column(name = "signal_type", length = 50)
    private String signalType;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Column(name = "override_pattern", length = 50)
    private String overridePattern;

    @Column(name = "campaign_score", nullable = false)
    private Integer campaignScore;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private CampaignSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CampaignStatus status;

    @Column(name = "matched_signature_id")
    private UUID matchedSignatureId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum CampaignSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum CampaignStatus {
        NEW, MONITORING, ACTIVE, CONTAINED, CLOSED
    }

    public FraudCampaign() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateIdentifier() {
        return templateIdentifier;
    }

    public void setTemplateIdentifier(String templateIdentifier) {
        this.templateIdentifier = templateIdentifier;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String getOverridePattern() {
        return overridePattern;
    }

    public void setOverridePattern(String overridePattern) {
        this.overridePattern = overridePattern;
    }

    public Integer getCampaignScore() {
        return campaignScore;
    }

    public void setCampaignScore(Integer campaignScore) {
        this.campaignScore = campaignScore;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public CampaignSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(CampaignSeverity severity) {
        this.severity = severity;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public UUID getMatchedSignatureId() {
        return matchedSignatureId;
    }

    public void setMatchedSignatureId(UUID matchedSignatureId) {
        this.matchedSignatureId = matchedSignatureId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
