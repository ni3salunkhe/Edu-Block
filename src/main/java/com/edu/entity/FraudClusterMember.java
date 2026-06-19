package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_cluster_members", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cluster_node", columnNames = {"cluster_id", "node_id"})
}, indexes = {
    @Index(name = "idx_member_cluster", columnList = "cluster_id"),
    @Index(name = "idx_member_node", columnList = "node_id")
})
public class FraudClusterMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cluster_id", nullable = false)
    private UUID clusterId;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "membership_score", nullable = false)
    private Double membershipScore = 1.0;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public FraudClusterMember() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClusterId() {
        return clusterId;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public Double getMembershipScore() {
        return membershipScore;
    }

    public void setMembershipScore(Double membershipScore) {
        this.membershipScore = membershipScore;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
