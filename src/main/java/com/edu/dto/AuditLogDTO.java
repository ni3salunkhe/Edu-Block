package com.edu.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AuditLogDTO {
    private UUID id;
    private Instant timestamp;
    private String actor;
    private String action;
    private String target;

    public AuditLogDTO() {
    }

    public AuditLogDTO(UUID id, Instant timestamp, String actor, String action, String target) {
        this.id = id;
        this.timestamp = timestamp;
        this.actor = actor;
        this.action = action;
        this.target = target;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLogDTO that = (AuditLogDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(actor, that.actor) &&
               Objects.equals(action, that.action) &&
               Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, actor, action, target);
    }

    @Override
    public String toString() {
        return "AuditLogDTO{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", actor='" + actor + '\'' +
                ", action='" + action + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
