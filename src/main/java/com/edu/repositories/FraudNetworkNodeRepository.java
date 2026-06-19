package com.edu.repositories;

import com.edu.entity.FraudNetworkNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudNetworkNodeRepository extends JpaRepository<FraudNetworkNode, UUID> {
    Optional<FraudNetworkNode> findByEntityIdAndNodeType(UUID entityId, FraudNetworkNode.NodeType nodeType);
    List<FraudNetworkNode> findTop1000ByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(int threshold);
}
