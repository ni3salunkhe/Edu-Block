package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_clusters", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cluster_fingerprint", columnNames = {"community_fingerprint"})
}, indexes = {
    @Index(name = "idx_cluster_score", columnList = "score")
})
public class FraudCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "density", nullable = false)
    private Double density;

    @Column(name = "weighted_density", nullable = false)
    private Double weightedDensity;

    @Column(name = "node_count", nullable = false)
    private Integer nodeCount;

    @Column(name = "edge_count", nullable = false)
    private Integer edgeCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ClusterStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "community_fingerprint", nullable = false, unique = true, length = 64)
    private String communityFingerprint;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    public enum ClusterStatus {
        ACTIVE, UNDER_INVESTIGATION, RESOLVED
    }

    public FraudCluster() {}

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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Double getDensity() {
        return density;
    }

    public void setDensity(Double density) {
        this.density = density;
    }

    public Double getWeightedDensity() {
        return weightedDensity;
    }

    public void setWeightedDensity(Double weightedDensity) {
        this.weightedDensity = weightedDensity;
    }

    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Integer getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(Integer edgeCount) {
        this.edgeCount = edgeCount;
    }

    public ClusterStatus getStatus() {
        return status;
    }

    public void setStatus(ClusterStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCommunityFingerprint() {
        return communityFingerprint;
    }

    public void setCommunityFingerprint(String communityFingerprint) {
        this.communityFingerprint = communityFingerprint;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
