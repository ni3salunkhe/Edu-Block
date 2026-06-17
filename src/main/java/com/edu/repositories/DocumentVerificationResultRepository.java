package com.edu.repositories;

import com.edu.entity.DocumentVerificationResult;
import com.edu.entity.DocumentVerificationResult.RiskLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentVerificationResultRepository extends JpaRepository<DocumentVerificationResult, UUID> {
    
    List<DocumentVerificationResult> findByRiskLevelInAndIsVerifiedFalse(Collection<RiskLevel> riskLevels, Pageable pageable);

    @Query("SELECT AVG(dvr.confidenceScore) FROM DocumentVerificationResult dvr")
    Double getAverageConfidenceScore();
    
    long countByIsVerifiedTrue();
    
    long countByIsVerifiedFalse();
}
