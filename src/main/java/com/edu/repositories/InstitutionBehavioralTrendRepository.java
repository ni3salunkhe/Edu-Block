package com.edu.repositories;

import com.edu.entity.InstitutionBehavioralTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface InstitutionBehavioralTrendRepository extends JpaRepository<InstitutionBehavioralTrend, UUID> {
    
    @Query("SELECT m FROM InstitutionBehavioralTrend m WHERE m.metricsWindowEnd = (SELECT MAX(m2.metricsWindowEnd) FROM InstitutionBehavioralTrend m2)")
    List<InstitutionBehavioralTrend> findLatestTrends();

    List<InstitutionBehavioralTrend> findByInstitutionIdOrderByMetricsWindowEndDesc(UUID institutionId);
}
