package com.edu.dto;

import java.util.Objects;
import java.util.UUID;

public class AIResult {

    /** Perceptual hash for visual similarity */
    private String perceptualHash;

    /** Similarity score with closest existing document (0.0 – 1.0) */
    private double similarityScore;

    /** ID of the most similar existing document (if any) */
    private UUID matchedDocumentId;

    /** OCR-extracted text (optional, can be truncated) */
    private String extractedText;

    /** AI confidence that document is genuine (0–100) */
    private int confidenceScore;

    /** Whether AI flags this document as suspicious */
    private boolean suspicious;

    /** Reason for flagging (human-readable) */
    private String flagReason;

    public AIResult() {
    }

    public AIResult(String perceptualHash, double similarityScore, UUID matchedDocumentId, 
                    String extractedText, int confidenceScore, boolean suspicious, String flagReason) {
        this.perceptualHash = perceptualHash;
        this.similarityScore = similarityScore;
        this.matchedDocumentId = matchedDocumentId;
        this.extractedText = extractedText;
        this.confidenceScore = confidenceScore;
        this.suspicious = suspicious;
        this.flagReason = flagReason;
    }

    public String getPerceptualHash() {
        return perceptualHash;
    }

    public void setPerceptualHash(String perceptualHash) {
        this.perceptualHash = perceptualHash;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public UUID getMatchedDocumentId() {
        return matchedDocumentId;
    }

    public void setMatchedDocumentId(UUID matchedDocumentId) {
        this.matchedDocumentId = matchedDocumentId;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public boolean isSuspicious() {
        return suspicious;
    }

    public void setSuspicious(boolean suspicious) {
        this.suspicious = suspicious;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIResult aiResult = (AIResult) o;
        return Double.compare(aiResult.similarityScore, similarityScore) == 0 &&
               confidenceScore == aiResult.confidenceScore &&
               suspicious == aiResult.suspicious &&
               Objects.equals(perceptualHash, aiResult.perceptualHash) &&
               Objects.equals(matchedDocumentId, aiResult.matchedDocumentId) &&
               Objects.equals(extractedText, aiResult.extractedText) &&
               Objects.equals(flagReason, aiResult.flagReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(perceptualHash, similarityScore, matchedDocumentId, 
                          extractedText, confidenceScore, suspicious, flagReason);
    }

    @Override
    public String toString() {
        return "AIResult{" +
                "perceptualHash='" + perceptualHash + '\'' +
                ", similarityScore=" + similarityScore +
                ", matchedDocumentId=" + matchedDocumentId +
                ", extractedText='" + extractedText + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", suspicious=" + suspicious +
                ", flagReason='" + flagReason + '\'' +
                '}';
    }
}

