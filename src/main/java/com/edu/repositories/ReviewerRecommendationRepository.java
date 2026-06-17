package com.edu.repositories;

import com.edu.entity.ReviewerRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewerRecommendationRepository extends JpaRepository<ReviewerRecommendation, UUID> {

    Optional<ReviewerRecommendation> findByDocumentIdAndState(UUID documentId, ReviewerRecommendation.RecommendationState state);

    List<ReviewerRecommendation> findByDocumentIdOrderByGenerationNumberDesc(UUID documentId);

    Optional<ReviewerRecommendation> findFirstByDocumentIdOrderByGenerationNumberDesc(UUID documentId);

    @Query("SELECT r FROM ReviewerRecommendation r WHERE r.state = 'ACTIVE'")
    List<ReviewerRecommendation> findAllActiveRecommendations();

    long countByRecommendation(ReviewerRecommendation.RecommendationType recommendation);
}
