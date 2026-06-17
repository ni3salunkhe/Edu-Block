package com.edu.repositories;

import com.edu.entity.InstitutionRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InstitutionRelationshipRepository extends JpaRepository<InstitutionRelationship, UUID> {
    List<InstitutionRelationship> findBySourceNodeId(UUID sourceNodeId);
    List<InstitutionRelationship> findByTargetNodeId(UUID targetNodeId);
}
