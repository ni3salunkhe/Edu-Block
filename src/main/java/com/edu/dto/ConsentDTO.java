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

    public ConsentDTO() {
    }

    public ConsentDTO(UUID id, UUID employerId, String employerName, EmployerConsent.Status status, Instant createdAt) {
        this.id = id;
        this.employerId = employerId;
        this.employerName = employerName;
        this.status = status;
        this.createdAt = createdAt;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsentDTO that = (ConsentDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(employerId, that.employerId) &&
               Objects.equals(employerName, that.employerName) &&
               status == that.status &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employerId, employerName, status, createdAt);
    }

    @Override
    public String toString() {
        return "ConsentDTO{" +
                "id=" + id +
                ", employerId=" + employerId +
                ", employerName='" + employerName + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
