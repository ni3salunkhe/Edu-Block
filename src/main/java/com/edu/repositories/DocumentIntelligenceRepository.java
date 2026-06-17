package com.edu.repositories;

import com.edu.entity.DocumentIntelligence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DocumentIntelligenceRepository extends JpaRepository<DocumentIntelligence, UUID> {
}
