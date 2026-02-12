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
@Table(name = "verifier_profiles")
public class VerifierProfile {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String authorityName;
    private String jurisdiction;

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

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerifierProfile that = (VerifierProfile) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(authorityName, that.authorityName) &&
               Objects.equals(jurisdiction, that.jurisdiction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, authorityName, jurisdiction);
    }

    @Override
    public String toString() {
        return "VerifierProfile{" +
                "userId=" + userId +
                ", authorityName='" + authorityName + '\'' +
                ", jurisdiction='" + jurisdiction + '\'' +
                '}';
    }
}
