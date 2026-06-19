package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_network_edges", uniqueConstraints = {
    @UniqueConstraint(name = "uq_source_target_type", columnNames = {"source_node_id", "target_node_id", "relationship_type"})
}, indexes = {
    @Index(name = "idx_edge_source", columnList = "source_node_id"),
    @Index(name = "idx_edge_target", columnList = "target_node_id"),
    @Index(name = "idx_edge_type", columnList = "relationship_type")
})
public class FraudNetworkEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_node_id", nullable = false)
    private UUID sourceNodeId;

    @Column(name = "target_node_id", nullable = false)
    private UUID targetNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 40)
    private RelationshipType relationshipType;

    @Column(name = "weight", nullable = false)
    private Double weight = 1.0;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public enum RelationshipType {
        ISSUED_BY,
        OWNED_BY,
        REVIEWED_BY,
        SHARES_OCR_FINGERPRINT,
        SHARES_VISUAL_HASH,
        SHARES_DOCUMENT_TEMPLATE,
        SHARES_TRANSACTION_PATTERN
    }

    public FraudNetworkEdge() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(UUID sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public UUID getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(UUID targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
