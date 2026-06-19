package com.edu.repositories;

import com.edu.entity.RecommendationEscalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecommendationEscalationRepository extends JpaRepository<RecommendationEscalation, UUID> {
    List<RecommendationEscalation> findByRecommendationIdAndActiveTrue(UUID recommendationId);
    List<RecommendationEscalation> findBySourceEntityIdAndSourceType(UUID sourceEntityId, String sourceType);
    Optional<RecommendationEscalation> findByRecommendationIdAndSourceEntityIdAndSourceType(UUID recommendationId, UUID sourceEntityId, String sourceType);
}
