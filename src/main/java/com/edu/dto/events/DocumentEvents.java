package com.edu.dto.events;

import java.util.UUID;

public class DocumentEvents {
    
    public static class DocumentUploadedEvent {
        private final UUID documentId;

        public DocumentUploadedEvent(UUID documentId) {
            this.documentId = documentId;
        }

        public UUID getDocumentId() {
            return documentId;
        }
    }

    public static class DocumentOcrCompletedEvent {
        private final UUID documentId;
        private final String rawText;

        public DocumentOcrCompletedEvent(UUID documentId, String rawText) {
            this.documentId = documentId;
            this.rawText = rawText;
        }

        public UUID getDocumentId() {
            return documentId;
        }

        public String getRawText() {
            return rawText;
        }
    }
}
