package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "graph_versions", indexes = {
    @Index(name = "idx_graph_ver", columnList = "version_number")
})
public class GraphVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "version_number", nullable = false)
    private Long versionNumber;

    @Column(name = "node_count", nullable = false)
    private Long nodeCount;

    @Column(name = "edge_count", nullable = false)
    private Long edgeCount;

    @Column(name = "cluster_count", nullable = false)
    private Long clusterCount = 0L;

    @Column(name = "state_hash", nullable = false, length = 64)
    private String stateHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public GraphVersion() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Long getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Long nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Long getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(Long edgeCount) {
        this.edgeCount = edgeCount;
    }

    public Long getClusterCount() {
        return clusterCount;
    }

    public void setClusterCount(Long clusterCount) {
        this.clusterCount = clusterCount;
    }

    public String getStateHash() {
        return stateHash;
    }

    public void setStateHash(String stateHash) {
        this.stateHash = stateHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
