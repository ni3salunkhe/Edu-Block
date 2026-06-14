package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

public class InstitutionIssuedDocumentDTO {
    private UUID documentId;
    private UUID studentId;
    private String studentName;
    private String documentType;
    private String credentialReference;
    private boolean blockchainAnchored;
    private String blockchainTransactionHash;
    private Instant issuedAt;

    public InstitutionIssuedDocumentDTO() {}

    public InstitutionIssuedDocumentDTO(UUID documentId, String studentName, String documentType,
                                        String credentialReference, boolean blockchainAnchored,
                                        String blockchainTransactionHash, Instant issuedAt) {
        this.documentId = documentId;
        this.studentName = studentName;
        this.documentType = documentType;
        this.credentialReference = credentialReference;
        this.blockchainAnchored = blockchainAnchored;
        this.blockchainTransactionHash = blockchainTransactionHash;
        this.issuedAt = issuedAt;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCredentialReference() {
        return credentialReference;
    }

    public void setCredentialReference(String credentialReference) {
        this.credentialReference = credentialReference;
    }

    public boolean isBlockchainAnchored() {
        return blockchainAnchored;
    }

    public void setBlockchainAnchored(boolean blockchainAnchored) {
        this.blockchainAnchored = blockchainAnchored;
    }

    public String getBlockchainTransactionHash() {
        return blockchainTransactionHash;
    }

    public void setBlockchainTransactionHash(String blockchainTransactionHash) {
        this.blockchainTransactionHash = blockchainTransactionHash;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}
