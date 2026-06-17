package com.edu.dto.events;

import java.util.UUID;

public class CredentialEvents {

    public static class CredentialIssuedEvent {
        private final UUID institutionId;
        private final UUID documentId;

        public CredentialIssuedEvent(UUID institutionId, UUID documentId) {
            this.institutionId = institutionId;
            this.documentId = documentId;
        }

        public UUID getInstitutionId() { return institutionId; }
        public UUID getDocumentId() { return documentId; }
    }
}
