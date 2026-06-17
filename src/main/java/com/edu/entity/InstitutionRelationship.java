package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "institution_relationships", indexes = {
    @Index(name = "idx_inst_rel_source", columnList = "sourceNodeId, sourceNodeType"),
    @Index(name = "idx_inst_rel_target", columnList = "targetNodeId, targetNodeType")
})
public class InstitutionRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sourceNodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NodeType sourceNodeType;

    @Column(nullable = false)
    private UUID targetNodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NodeType targetNodeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RelationshipType relationshipType;

    @Column(nullable = false)
    private Instant createdAt;

    public enum NodeType {
        INSTITUTION,
        STUDENT,
        CREDENTIAL,
        FRAUD_CLUSTER
    }

    public enum RelationshipType {
        ISSUED_TO,
        ASSOCIATED_WITH,
        FLAGGED_IN,
        AFFILIATED_COLLEGE
    }

    public InstitutionRelationship() {}

    public InstitutionRelationship(UUID sourceNodeId, NodeType sourceNodeType, UUID targetNodeId, NodeType targetNodeType, RelationshipType relationshipType, Instant createdAt) {
        this.sourceNodeId = sourceNodeId;
        this.sourceNodeType = sourceNodeType;
        this.targetNodeId = targetNodeId;
        this.targetNodeType = targetNodeType;
        this.relationshipType = relationshipType;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(UUID sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public NodeType getSourceNodeType() { return sourceNodeType; }
    public void setSourceNodeType(NodeType sourceNodeType) { this.sourceNodeType = sourceNodeType; }

    public UUID getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(UUID targetNodeId) { this.targetNodeId = targetNodeId; }

    public NodeType getTargetNodeType() { return targetNodeType; }
    public void setTargetNodeType(NodeType targetNodeType) { this.targetNodeType = targetNodeType; }

    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
