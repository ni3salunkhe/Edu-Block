package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_signatures", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"})
})
public class FraudSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "pattern_rules_json", nullable = false, columnDefinition = "TEXT")
    private String patternRulesJson;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "historical_campaigns_count", nullable = false)
    private Integer historicalCampaignsCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public FraudSignature() {}

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

    public String getPatternRulesJson() {
        return patternRulesJson;
    }

    public void setPatternRulesJson(String patternRulesJson) {
        this.patternRulesJson = patternRulesJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getHistoricalCampaignsCount() {
        return historicalCampaignsCount;
    }

    public void setHistoricalCampaignsCount(Integer historicalCampaignsCount) {
        this.historicalCampaignsCount = historicalCampaignsCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
