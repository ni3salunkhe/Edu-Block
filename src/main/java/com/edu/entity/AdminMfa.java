package com.edu.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_mfa")
public class AdminMfa {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String secret; // encrypted base32

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int failedAttempts;

    private Instant lastVerifiedAt;

    private Instant createdAt;

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

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Instant getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    public void setLastVerifiedAt(Instant lastVerifiedAt) {
        this.lastVerifiedAt = lastVerifiedAt;
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
        AdminMfa adminMfa = (AdminMfa) o;
        return enabled == adminMfa.enabled &&
               failedAttempts == adminMfa.failedAttempts &&
               Objects.equals(userId, adminMfa.userId) &&
               Objects.equals(secret, adminMfa.secret) &&
               Objects.equals(lastVerifiedAt, adminMfa.lastVerifiedAt) &&
               Objects.equals(createdAt, adminMfa.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, secret, enabled, failedAttempts, lastVerifiedAt, createdAt);
    }

    @Override
    public String toString() {
        return "AdminMfa{" +
                "userId=" + userId +
                ", enabled=" + enabled +
                ", failedAttempts=" + failedAttempts +
                ", lastVerifiedAt=" + lastVerifiedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
