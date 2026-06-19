package com.edu.repositories;

import com.edu.entity.RecommendationPerformanceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationPerformanceMetricRepository extends JpaRepository<RecommendationPerformanceMetric, UUID> {
    
    @Query("SELECT m FROM RecommendationPerformanceMetric m WHERE m.metricsWindowEnd = (SELECT MAX(m2.metricsWindowEnd) FROM RecommendationPerformanceMetric m2)")
    List<RecommendationPerformanceMetric> findLatestMetrics();

    List<RecommendationPerformanceMetric> findByRecommendationTypeOrderByMetricsWindowEndDesc(String recommendationType);
}
