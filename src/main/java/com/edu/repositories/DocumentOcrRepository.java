package com.edu.repositories;

import com.edu.entity.DocumentOcr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentOcrRepository extends JpaRepository<DocumentOcr, UUID> {
    
    // Finds OCR records matching a status, with retryCount under the limit and lastAttemptAt before the delay threshold
    List<DocumentOcr> findByOcrStatusAndRetryCountLessThanAndLastAttemptAtBefore(
        DocumentOcr.OcrStatus ocrStatus, int maxRetries, Instant threshold
    );
}
