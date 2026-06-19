package com.edu.repositories;

import com.edu.entity.VerifierQualityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface VerifierQualityMetricsRepository extends JpaRepository<VerifierQualityMetrics, UUID> {
    
    @Query("SELECT m FROM VerifierQualityMetrics m WHERE m.metricsWindowEnd = (SELECT MAX(m2.metricsWindowEnd) FROM VerifierQualityMetrics m2)")
    List<VerifierQualityMetrics> findLatestMetrics();

    List<VerifierQualityMetrics> findByVerifierIdOrderByMetricsWindowEndDesc(UUID verifierId);
}
