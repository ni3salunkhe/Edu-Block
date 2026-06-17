package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "institution_trust_history", indexes = {
    @Index(name = "idx_inst_history_id_time", columnList = "institution_id, timestamp")
})
public class InstitutionTrustHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(nullable = false)
    private Integer previousScore;

    @Column(nullable = false)
    private Integer newScore;

    @Column(nullable = false)
    private Integer scoreDelta; // Explicit delta (e.g. -15)

    @Column(nullable = false, length = 50)
    private String changeTriggerType; // e.g. "FRAUD_SPIKE", "ACCREDITATION_CHANGE", "BATCH_RUN"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason; // Detailed context

    @Column(nullable = false)
    private Instant timestamp;

    public InstitutionTrustHistory() {}

    public InstitutionTrustHistory(UUID institutionId, Integer previousScore, Integer newScore, Integer scoreDelta, String changeTriggerType, String reason, Instant timestamp) {
        this.institutionId = institutionId;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.scoreDelta = scoreDelta;
        this.changeTriggerType = changeTriggerType;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public Integer getPreviousScore() { return previousScore; }
    public void setPreviousScore(Integer previousScore) { this.previousScore = previousScore; }

    public Integer getNewScore() { return newScore; }
    public void setNewScore(Integer newScore) { this.newScore = newScore; }

    public Integer getScoreDelta() { return scoreDelta; }
    public void setScoreDelta(Integer scoreDelta) { this.scoreDelta = scoreDelta; }

    public String getChangeTriggerType() { return changeTriggerType; }
    public void setChangeTriggerType(String changeTriggerType) { this.changeTriggerType = changeTriggerType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
