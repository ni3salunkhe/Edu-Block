package com.edu.scheduler;

import com.edu.dto.events.DocumentEvents.DocumentUploadedEvent;
import com.edu.entity.DocumentOcr;
import com.edu.repositories.DocumentOcrRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DocumentOcrRetryScheduler {

    private final DocumentOcrRepository ocrRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${edublock.ocr.max-retries:3}")
    private int maxRetries;

    @Value("${edublock.ocr.retry-delay-minutes:5}")
    private int retryDelayMinutes;

    public DocumentOcrRetryScheduler(DocumentOcrRepository ocrRepository, ApplicationEventPublisher eventPublisher) {
        this.ocrRepository = ocrRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${edublock.ocr.retry-check-interval-ms:60000}")
    public void retryFailedOcrJobs() {
        Instant threshold = Instant.now().minus(retryDelayMinutes, ChronoUnit.MINUTES);
        
        // Find jobs in FAILED state (failed attempt) that are due for retry
        List<DocumentOcr> retryCandidates = ocrRepository.findByOcrStatusAndRetryCountLessThanAndLastAttemptAtBefore(
            DocumentOcr.OcrStatus.FAILED, maxRetries, threshold
        );

        for (DocumentOcr ocr : retryCandidates) {
            ocr.setOcrStatus(DocumentOcr.OcrStatus.PROCESSING);
            ocrRepository.save(ocr);
            
            // Republish the upload event to trigger the listener
            eventPublisher.publishEvent(new DocumentUploadedEvent(ocr.getDocumentId()));
        }
    }
}
