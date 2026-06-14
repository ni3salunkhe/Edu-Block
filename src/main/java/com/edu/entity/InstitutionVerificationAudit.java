package com.edu.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "institution_verification_audits")
public class InstitutionVerificationAudit {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionTrustLevel oldTrustLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionTrustLevel newTrustLevel;

    @Column(nullable = false)
    private UUID changedBy;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private Instant changedAt;

    @PrePersist
    protected void onCreate() {
        changedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public InstitutionTrustLevel getOldTrustLevel() {
        return oldTrustLevel;
    }

    public void setOldTrustLevel(InstitutionTrustLevel oldTrustLevel) {
        this.oldTrustLevel = oldTrustLevel;
    }

    public InstitutionTrustLevel getNewTrustLevel() {
        return newTrustLevel;
    }

    public void setNewTrustLevel(InstitutionTrustLevel newTrustLevel) {
        this.newTrustLevel = newTrustLevel;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionVerificationAudit that = (InstitutionVerificationAudit) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(institution, that.institution) &&
               oldTrustLevel == that.oldTrustLevel &&
               newTrustLevel == that.newTrustLevel &&
               Objects.equals(changedBy, that.changedBy) &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(changedAt, that.changedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, institution, oldTrustLevel, newTrustLevel, changedBy, reason, changedAt);
    }

    @Override
    public String toString() {
        return "InstitutionVerificationAudit{" +
                "id=" + id +
                ", oldTrustLevel=" + oldTrustLevel +
                ", newTrustLevel=" + newTrustLevel +
                ", changedBy=" + changedBy +
                ", reason='" + reason + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}
