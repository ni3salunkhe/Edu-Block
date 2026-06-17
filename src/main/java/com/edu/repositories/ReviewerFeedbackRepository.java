package com.edu.repositories;

import com.edu.entity.ReviewerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewerFeedbackRepository extends JpaRepository<ReviewerFeedback, UUID> {

    Optional<ReviewerFeedback> findByRecommendationId(UUID recommendationId);

    long countByIsOverride(boolean isOverride);

    // (D) Override rate by institution — joins feedback → recommendation → document
    @Query("""
            SELECT d.issuingInstitutionId, COUNT(f.id), SUM(CASE WHEN f.isOverride = true THEN 1 ELSE 0 END)
            FROM ReviewerFeedback f
            JOIN ReviewerRecommendation r ON f.recommendationId = r.id
            JOIN Document d ON r.documentId = d.id
            WHERE d.issuingInstitutionId IS NOT NULL
            GROUP BY d.issuingInstitutionId
            """)
    List<Object[]> countOverridesByInstitution();

    // (D) Override rate by fraud signal type — joins feedback → recommendation → document → fraud signals
    @Query("""
            SELECT fs.signalType, COUNT(DISTINCT f.id), SUM(CASE WHEN f.isOverride = true THEN 1 ELSE 0 END)
            FROM ReviewerFeedback f
            JOIN ReviewerRecommendation r ON f.recommendationId = r.id
            JOIN Document d ON r.documentId = d.id
            JOIN FraudSignal fs ON fs.document.id = d.id
            GROUP BY fs.signalType
            """)
    List<Object[]> countOverridesByFraudType();

    // (D) Override rate by verifier
    @Query("""
            SELECT f.verifierId, COUNT(f.id), SUM(CASE WHEN f.isOverride = true THEN 1 ELSE 0 END)
            FROM ReviewerFeedback f
            GROUP BY f.verifierId
            """)
    List<Object[]> countOverridesByVerifier();

    // Average review time in seconds (completedAt - assignedAt) — native SQL because
    // JPQL EXTRACT() requires TEMPORAL but Instant subtraction yields a Duration type
    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (completed_at - assigned_at)))
            FROM reviewer_recommendations
            WHERE assigned_at IS NOT NULL AND completed_at IS NOT NULL
            """, nativeQuery = true)
    Double averageReviewTimeSeconds();
}
