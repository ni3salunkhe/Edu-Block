package com.edu.repositories;

import com.edu.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

	boolean existsBySha256Hash(String sha256Hash);

    Optional<Document> findBySha256Hash(String sha256Hash);

    List<Document> findByPerceptualHashIsNotNull();

    List<Document> findByStatus(Document.Status status);

    List<Document> findByOwnerId(UUID ownerId);

    List<Document> findByStatusIn(List<Document.Status> statuses);

    @Query("""
        SELECT d FROM Document d
        WHERE d.blockchainTx IS NOT NULL
    """)
    List<Document> findAnchoredDocuments();

    long countByOwnerId(UUID ownerId);

    long countByStatus(Document.Status status);
    
    long countByStatusIn(List<Document.Status> statuses);
}
