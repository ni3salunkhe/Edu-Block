package com.edu.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionTrustLevel;

public class InstitutionResponseDTO {
    private UUID id;
    private String name;
    private String code;
    private String website;
    private String officialEmail;
    private String city;
    private String state;
    private String country;
    private InstitutionTrustLevel trustLevel;
    private InstitutionStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public InstitutionResponseDTO() {}

    public InstitutionResponseDTO(UUID id, String name, String code, String website, String officialEmail,
                                  String city, String state, String country,
                                  InstitutionTrustLevel trustLevel, InstitutionStatus status,
                                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.website = website;
        this.officialEmail = officialEmail;
        this.city = city;
        this.state = state;
        this.country = country;
        this.trustLevel = trustLevel;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public InstitutionTrustLevel getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(InstitutionTrustLevel trustLevel) {
        this.trustLevel = trustLevel;
    }

    public InstitutionStatus getStatus() {
        return status;
    }

    public void setStatus(InstitutionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionResponseDTO that = (InstitutionResponseDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(code, that.code) &&
               Objects.equals(website, that.website) &&
               Objects.equals(officialEmail, that.officialEmail) &&
               Objects.equals(city, that.city) &&
               Objects.equals(state, that.state) &&
               Objects.equals(country, that.country) &&
               trustLevel == that.trustLevel &&
               status == that.status &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, code, website, officialEmail, city, state, country, trustLevel, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "InstitutionResponseDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", website='" + website + '\'' +
                ", officialEmail='" + officialEmail + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", trustLevel=" + trustLevel +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
