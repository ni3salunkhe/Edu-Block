package com.edu.dto.events;

import java.util.UUID;

public class VerificationEvents {

    public static class DocumentVerificationCompletedEvent {
        private final UUID documentId;
        private final UUID institutionId;
        private final int confidenceScore;

        public DocumentVerificationCompletedEvent(UUID documentId, UUID institutionId, int confidenceScore) {
            this.documentId = documentId;
            this.institutionId = institutionId;
            this.confidenceScore = confidenceScore;
        }

        public UUID getDocumentId() { return documentId; }
        public UUID getInstitutionId() { return institutionId; }
        public int getConfidenceScore() { return confidenceScore; }
    }

    public static class FraudSignalDetectedEvent {
        private final UUID documentId;
        private final UUID institutionId;
        private final String signalType;
        private final String severity;

        public FraudSignalDetectedEvent(UUID documentId, UUID institutionId, String signalType, String severity) {
            this.documentId = documentId;
            this.institutionId = institutionId;
            this.signalType = signalType;
            this.severity = severity;
        }

        public UUID getDocumentId() { return documentId; }
        public UUID getInstitutionId() { return institutionId; }
        public String getSignalType() { return signalType; }
        public String getSeverity() { return severity; }
    }
}
