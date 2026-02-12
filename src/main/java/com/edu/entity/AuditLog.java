package com.edu.entity;

import java.time.Instant;
import java.util.Objects;
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

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public ActorRole getActorRole() {
        return actorRole;
    }

    public void setActorRole(ActorRole actorRole) {
        this.actorRole = actorRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id) &&
               Objects.equals(actorUserId, auditLog.actorUserId) &&
               actorRole == auditLog.actorRole &&
               Objects.equals(action, auditLog.action) &&
               Objects.equals(targetType, auditLog.targetType) &&
               Objects.equals(targetId, auditLog.targetId) &&
               Objects.equals(metadata, auditLog.metadata) &&
               Objects.equals(ipAddress, auditLog.ipAddress) &&
               Objects.equals(createdAt, auditLog.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, actorUserId, actorRole, action, targetType, 
                          targetId, metadata, ipAddress, createdAt);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", actorUserId=" + actorUserId +
                ", actorRole=" + actorRole +
                ", action='" + action + '\'' +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
