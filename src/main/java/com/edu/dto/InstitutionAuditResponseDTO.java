package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

import com.edu.entity.InstitutionTrustLevel;

public class InstitutionAuditResponseDTO {
    private InstitutionTrustLevel oldTrustLevel;
    private InstitutionTrustLevel newTrustLevel;
    private String reason;
    private UUID changedBy;
    private Instant changedAt;

    public InstitutionAuditResponseDTO() {}

    public InstitutionAuditResponseDTO(InstitutionTrustLevel oldTrustLevel, InstitutionTrustLevel newTrustLevel,
                                     String reason, UUID changedBy, Instant changedAt) {
        this.oldTrustLevel = oldTrustLevel;
        this.newTrustLevel = newTrustLevel;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}
