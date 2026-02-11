package com.edu.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
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
}
