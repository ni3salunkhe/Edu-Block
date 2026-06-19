package com.edu.repositories;

import com.edu.entity.FraudCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudClusterRepository extends JpaRepository<FraudCluster, UUID> {
    Optional<FraudCluster> findByCommunityFingerprint(String communityFingerprint);
    List<FraudCluster> findByStatusAndLastSeenAtBefore(FraudCluster.ClusterStatus status, Instant threshold);
}
