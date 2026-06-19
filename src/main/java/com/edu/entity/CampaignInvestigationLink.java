package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaign_investigation_links", uniqueConstraints = {
    @UniqueConstraint(name = "uq_campaign_investigation", columnNames = {"campaign_id", "investigation_case_id"})
}, indexes = {
    @Index(name = "idx_link_campaign", columnList = "campaign_id"),
    @Index(name = "idx_link_investigation", columnList = "investigation_case_id")
})
public class CampaignInvestigationLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "investigation_case_id", nullable = false)
    private UUID investigationCaseId;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @Column(name = "linked_by", nullable = false, length = 100)
    private String linkedBy;

    public CampaignInvestigationLink() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(UUID campaignId) {
        this.campaignId = campaignId;
    }

    public UUID getInvestigationCaseId() {
        return investigationCaseId;
    }

    public void setInvestigationCaseId(UUID investigationCaseId) {
        this.investigationCaseId = investigationCaseId;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }

    public String getLinkedBy() {
        return linkedBy;
    }

    public void setLinkedBy(String linkedBy) {
        this.linkedBy = linkedBy;
    }
}
