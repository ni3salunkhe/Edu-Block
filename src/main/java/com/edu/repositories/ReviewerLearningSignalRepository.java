package com.edu.repositories;

import com.edu.entity.ReviewerLearningSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewerLearningSignalRepository extends JpaRepository<ReviewerLearningSignal, UUID> {
    List<ReviewerLearningSignal> findByCreatedAtBetween(Instant start, Instant end);
    List<ReviewerLearningSignal> findByInstitutionIdAndCreatedAtAfter(UUID institutionId, Instant since);
}
