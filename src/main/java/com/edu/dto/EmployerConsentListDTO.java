package com.edu.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.edu.entity.EmployerConsent;

public class EmployerConsentListDTO {
    private UUID id;
    private String individualName;
    private EmployerConsent.Status status;
    private Instant createdAt;

    public EmployerConsentListDTO() {
    }

    public EmployerConsentListDTO(UUID id, String individualName, EmployerConsent.Status status) {
        this.id = id;
        this.individualName = individualName;
        this.status = status;
    }

    public EmployerConsentListDTO(UUID id, String individualName, EmployerConsent.Status status, Instant createdAt) {
        this.id = id;
        this.individualName = individualName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
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
        EmployerConsentListDTO that = (EmployerConsentListDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(individualName, that.individualName) &&
               status == that.status &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, individualName, status, createdAt);
    }

    @Override
    public String toString() {
        return "EmployerConsentListDTO{" +
                "id=" + id +
                ", individualName='" + individualName + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
