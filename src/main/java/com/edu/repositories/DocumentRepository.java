package com.edu.repositories;

import com.edu.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /* ---------- DUPLICATE DETECTION ---------- */

    // Exact duplicate detection (SHA-256)
    boolean existsBySha256Hash(String sha256Hash);

    Optional<Document> findBySha256Hash(String sha256Hash);

    /* ---------- AI / SIMILARITY ---------- */

    // Used by SimilarityService (early-stage O(N))
    List<Document> findByPerceptualHashIsNotNull();

    /* ---------- VERIFIER DASHBOARD ---------- */

    List<Document> findByStatus(Document.Status status);

    /* ---------- USER / OWNER ---------- */

    List<Document> findByOwnerId(UUID ownerId);

    // For verifier dashboard (AI_VERIFIED + AI_FLAGGED)
    List<Document> findByStatusIn(List<Document.Status> statuses);

    /* ---------- BLOCKCHAIN ---------- */

    @Query("""
        SELECT d FROM Document d
        WHERE d.blockchainTx IS NOT NULL
    """)
    List<Document> findAnchoredDocuments();
}
