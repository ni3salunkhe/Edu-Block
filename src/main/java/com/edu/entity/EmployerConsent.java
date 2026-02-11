package com.edu.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
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
}
