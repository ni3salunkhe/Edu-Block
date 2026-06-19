package com.edu.repositories;

import com.edu.entity.FraudNetworkEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FraudNetworkEdgeRepository extends JpaRepository<FraudNetworkEdge, UUID> {
    List<FraudNetworkEdge> findBySourceNodeId(UUID sourceNodeId);
    List<FraudNetworkEdge> findBySourceNodeIdOrTargetNodeId(UUID sourceNodeId, UUID targetNodeId);
    List<FraudNetworkEdge> findBySourceNodeIdInOrTargetNodeIdIn(java.util.Collection<UUID> sourceIds, java.util.Collection<UUID> targetIds);
    boolean existsBySourceNodeIdAndTargetNodeIdAndRelationshipType(UUID sourceNodeId, UUID targetNodeId, FraudNetworkEdge.RelationshipType relationshipType);
}
