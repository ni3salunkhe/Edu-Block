package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_ocr")
public class DocumentOcr {

    @Id
    private UUID documentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OcrStatus ocrStatus;

    private int retryCount;
    private Instant lastAttemptAt;

    @Column(length = 500)
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String ocrMetadataJson;

    private Instant processedAt;

    public enum OcrStatus {
        PENDING,       // Queued, not attempted yet
        PROCESSING,    // Job currently running
        SUCCESS,       // OCR completed successfully
        FAILED         // Attempt failed (eligible for retry unless max reached)
    }

    public DocumentOcr() {
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public OcrStatus getOcrStatus() {
        return ocrStatus;
    }

    public void setOcrStatus(OcrStatus ocrStatus) {
        this.ocrStatus = ocrStatus;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Instant lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getOcrMetadataJson() {
        return ocrMetadataJson;
    }

    public void setOcrMetadataJson(String ocrMetadataJson) {
        this.ocrMetadataJson = ocrMetadataJson;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
