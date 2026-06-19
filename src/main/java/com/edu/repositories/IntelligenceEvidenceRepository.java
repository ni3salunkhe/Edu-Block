package com.edu.repositories;

import com.edu.entity.IntelligenceEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntelligenceEvidenceRepository extends JpaRepository<IntelligenceEvidence, UUID> {
    List<IntelligenceEvidence> findBySourceIdAndSourceType(UUID sourceId, String sourceType);
}
