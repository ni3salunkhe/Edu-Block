package com.edu.dto;

import com.edu.entity.DocumentVerificationResult.ConfidenceLevel;
import com.edu.entity.DocumentVerificationResult.RiskLevel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DocumentVerificationResultDTO {

    private UUID documentId;
    private int confidenceScore;
    private ConfidenceLevel confidenceLevel;
    private RiskLevel riskLevel;
    private boolean isVerified;
    private String verificationModelVersion;
    private String ocrTextHash;
    private Instant processedAt;
    private List<MapExplanation> explanations;
    private List<FraudSignalDTO> fraudSignals;

    public DocumentVerificationResultDTO() {}

    public static class MapExplanation {
        private String ruleName;
        private int scoreModifier;
        private String description;
        private boolean triggered;

        public MapExplanation() {}

        public MapExplanation(String ruleName, int scoreModifier, String description, boolean triggered) {
            this.ruleName = ruleName;
            this.scoreModifier = scoreModifier;
            this.description = description;
            this.triggered = triggered;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public int getScoreModifier() {
            return scoreModifier;
        }

        public void setScoreModifier(int scoreModifier) {
            this.scoreModifier = scoreModifier;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isTriggered() {
            return triggered;
        }

        public void setTriggered(boolean triggered) {
            this.triggered = triggered;
        }
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean verified) {
        isVerified = verified;
    }

    public String getVerificationModelVersion() {
        return verificationModelVersion;
    }

    public void setVerificationModelVersion(String verificationModelVersion) {
        this.verificationModelVersion = verificationModelVersion;
    }

    public String getOcrTextHash() {
        return ocrTextHash;
    }

    public void setOcrTextHash(String ocrTextHash) {
        this.ocrTextHash = ocrTextHash;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public List<MapExplanation> getExplanations() {
        return explanations;
    }

    public void setExplanations(List<MapExplanation> explanations) {
        this.explanations = explanations;
    }

    public List<FraudSignalDTO> getFraudSignals() {
        return fraudSignals;
    }

    public void setFraudSignals(List<FraudSignalDTO> fraudSignals) {
        this.fraudSignals = fraudSignals;
    }
}
