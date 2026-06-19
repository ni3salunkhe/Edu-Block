package com.edu.repositories;

import com.edu.entity.FraudClusterMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface FraudClusterMemberRepository extends JpaRepository<FraudClusterMember, UUID> {
    List<FraudClusterMember> findByClusterId(UUID clusterId);
    List<FraudClusterMember> findByNodeId(UUID nodeId);

    @Transactional
    void deleteByClusterId(UUID clusterId);
}
