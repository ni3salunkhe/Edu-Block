package com.edu.repositories;

import com.edu.entity.RulePerformanceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RulePerformanceMetricRepository extends JpaRepository<RulePerformanceMetric, UUID> {
    
    @Query("SELECT m FROM RulePerformanceMetric m WHERE m.metricsWindowEnd = (SELECT MAX(m2.metricsWindowEnd) FROM RulePerformanceMetric m2)")
    List<RulePerformanceMetric> findLatestMetrics();

    List<RulePerformanceMetric> findByRuleNameOrderByMetricsWindowEndDesc(String ruleName);
}
