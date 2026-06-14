package com.edu.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.edu.entity.EmployerConsent;

public class ConsentDTO {
    private UUID id;
    private UUID employerId;
    private String employerName;
    private EmployerConsent.Status status;
    private Instant createdAt;
    private Instant validUntil;
    private Instant decisionAt;

    public ConsentDTO() {
    }

    public ConsentDTO(UUID id, UUID employerId, String employerName, EmployerConsent.Status status, Instant createdAt) {
        this.id = id;
        this.employerId = employerId;
        this.employerName = employerName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public ConsentDTO(UUID id, UUID employerId, String employerName, EmployerConsent.Status status,
                      Instant createdAt, Instant validUntil, Instant decisionAt) {
        this.id = id;
        this.employerId = employerId;
        this.employerName = employerName;
        this.status = status;
        this.createdAt = createdAt;
        this.validUntil = validUntil;
        this.decisionAt = decisionAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEmployerId() {
        return employerId;
    }

    public void setEmployerId(UUID employerId) {
        this.employerId = employerId;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public EmployerConsent.Status getStatus() {
        return status;
    }

    public void setStatus(EmployerConsent.Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getDecisionAt() {
        return decisionAt;
    }

    public void setDecisionAt(Instant decisionAt) {
        this.decisionAt = decisionAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentDTO that = (ConsentDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(employerId, that.employerId) &&
               Objects.equals(employerName, that.employerName) &&
               status == that.status &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(validUntil, that.validUntil) &&
               Objects.equals(decisionAt, that.decisionAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employerId, employerName, status, createdAt, validUntil, decisionAt);
    }

    @Override
    public String toString() {
        return "ConsentDTO{" +
                "id=" + id +
                ", employerId=" + employerId +
                ", employerName='" + employerName + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", validUntil=" + validUntil +
                ", decisionAt=" + decisionAt +
                '}';
    }
}
