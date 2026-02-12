package com.edu.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "employer_consents",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"employer_id", "owner_id"}
       ))
public class EmployerConsent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID employerId;

    @Column(nullable = false)
    private UUID ownerId; // Individual

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant validFrom;
    private Instant validUntil;

    private Instant createdAt;

    public enum Status {
        REQUESTED,
        APPROVED,
        REVOKED,
        EXPIRED,
        REJECTED
    }

    // Getters and Setters
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployerConsent that = (EmployerConsent) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(employerId, that.employerId) &&
               Objects.equals(ownerId, that.ownerId) &&
               status == that.status &&
               Objects.equals(validFrom, that.validFrom) &&
               Objects.equals(validUntil, that.validUntil) &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employerId, ownerId, status, validFrom, validUntil, createdAt);
    }

    @Override
    public String toString() {
        return "EmployerConsent{" +
                "id=" + id +
                ", employerId=" + employerId +
                ", ownerId=" + ownerId +
                ", status=" + status +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", createdAt=" + createdAt +
                '}';
    }
}
