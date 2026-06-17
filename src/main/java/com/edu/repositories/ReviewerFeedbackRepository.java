package com.edu.repositories;

import com.edu.entity.ReviewerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewerFeedbackRepository extends JpaRepository<ReviewerFeedback, UUID> {

    Optional<ReviewerFeedback> findByRecommendationId(UUID recommendationId);

    long countByIsOverride(boolean isOverride);
}
