package com.edu.dto;

import java.util.Objects;
import java.util.UUID;

import com.edu.entity.EmployerConsent;

public class EmployerConsentListDTO {
    private UUID id;
    private String individualName;
    private EmployerConsent.Status status;

    public EmployerConsentListDTO() {
    }

    public EmployerConsentListDTO(UUID id, String individualName, EmployerConsent.Status status) {
        this.id = id;
        this.individualName = individualName;
        this.status = status;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployerConsentListDTO that = (EmployerConsentListDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(individualName, that.individualName) &&
               status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, individualName, status);
    }

    @Override
    public String toString() {
        return "EmployerConsentListDTO{" +
                "id=" + id +
                ", individualName='" + individualName + '\'' +
                ", status=" + status +
                '}';
    }
}
