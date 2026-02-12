package com.edu.entity;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "institution_profiles")
public class InstitutionProfile {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String institutionName;
    private String accreditationId;

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getAccreditationId() {
        return accreditationId;
    }

    public void setAccreditationId(String accreditationId) {
        this.accreditationId = accreditationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionProfile that = (InstitutionProfile) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(institutionName, that.institutionName) &&
               Objects.equals(accreditationId, that.accreditationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, institutionName, accreditationId);
    }

    @Override
    public String toString() {
        return "InstitutionProfile{" +
                "userId=" + userId +
                ", institutionName='" + institutionName + '\'' +
                ", accreditationId='" + accreditationId + '\'' +
                '}';
    }
}
