package com.edu.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID actorUserId;   // admin or verifier

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActorRole actorRole; // ADMIN / VERIFIER

    @Column(nullable = false)
    private String action;       // CREATE_VERIFIER, APPROVE_DOCUMENT

    @Column(nullable = false)
    private String targetType;   // USER, DOCUMENT

    @Column(nullable = false)
    private UUID targetId;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;     // optional context

    private String ipAddress;

    @Column(nullable = false)
    private Instant createdAt;

    public enum ActorRole {
        ADMIN,
        VERIFIER,
        EMPLOYER,
        INDIVIDUAL
    }
}
