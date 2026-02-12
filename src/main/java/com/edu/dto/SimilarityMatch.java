package com.edu.dto;

import java.util.Objects;
import java.util.UUID;

public class SimilarityMatch {

    private UUID documentId;     // closest matching doc
    private double score;        // similarity (0.0 – 1.0)
    private String reason;       // explanation

    public SimilarityMatch() {
    }

    public SimilarityMatch(UUID documentId, double score, String reason) {
        this.documentId = documentId;
        this.score = score;
        this.reason = reason;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimilarityMatch that = (SimilarityMatch) o;
        return Double.compare(that.score, score) == 0 &&
               Objects.equals(documentId, that.documentId) &&
               Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, score, reason);
    }

    @Override
    public String toString() {
        return "SimilarityMatch{" +
                "documentId=" + documentId +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                '}';
    }
}

