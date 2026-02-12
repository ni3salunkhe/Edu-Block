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
@Table(name = "employer_profiles")
public class EmployerProfile {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String companyName;
    private String companyDomain;
    private String registrationNumber;

    private boolean verified;

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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDomain() {
        return companyDomain;
    }

    public void setCompanyDomain(String companyDomain) {
        this.companyDomain = companyDomain;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployerProfile that = (EmployerProfile) o;
        return verified == that.verified &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(companyName, that.companyName) &&
               Objects.equals(companyDomain, that.companyDomain) &&
               Objects.equals(registrationNumber, that.registrationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, companyName, companyDomain, registrationNumber, verified);
    }

    @Override
    public String toString() {
        return "EmployerProfile{" +
                "userId=" + userId +
                ", companyName='" + companyName + '\'' +
                ", companyDomain='" + companyDomain + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", verified=" + verified +
                '}';
    }

}
