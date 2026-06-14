package com.edu.dto;

import com.edu.entity.DocumentSource;
import com.edu.entity.Document;
import com.edu.entity.InstitutionTrustLevel;
import java.util.UUID;

public class DocumentTrustResponseDTO {
    private UUID documentId;
    private String documentType;
    private DocumentSource documentSource;
    private Document.Status verificationStatus;
    private UUID issuingInstitutionId;
    private String issuingInstitutionName;
    private InstitutionTrustLevel institutionTrustLevel;
    private boolean institutionVerified;
    private String credentialReference;
    private boolean blockchainAnchored;
    private String transactionHash;
    private TrustLevel trustScore;

    public DocumentTrustResponseDTO() {}

    public DocumentTrustResponseDTO(UUID documentId, String documentType, DocumentSource documentSource,
                                    Document.Status verificationStatus, UUID issuingInstitutionId,
                                    String issuingInstitutionName, InstitutionTrustLevel institutionTrustLevel,
                                    boolean institutionVerified, String credentialReference, boolean blockchainAnchored,
                                    String transactionHash, TrustLevel trustScore) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.documentSource = documentSource;
        this.verificationStatus = verificationStatus;
        this.issuingInstitutionId = issuingInstitutionId;
        this.issuingInstitutionName = issuingInstitutionName;
        this.institutionTrustLevel = institutionTrustLevel;
        this.institutionVerified = institutionVerified;
        this.credentialReference = credentialReference;
        this.blockchainAnchored = blockchainAnchored;
        this.transactionHash = transactionHash;
        this.trustScore = trustScore;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public DocumentSource getDocumentSource() {
        return documentSource;
    }

    public void setDocumentSource(DocumentSource documentSource) {
        this.documentSource = documentSource;
    }

    public Document.Status getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(Document.Status verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public UUID getIssuingInstitutionId() {
        return issuingInstitutionId;
    }

    public void setIssuingInstitutionId(UUID issuingInstitutionId) {
        this.issuingInstitutionId = issuingInstitutionId;
    }

    public String getIssuingInstitutionName() {
        return issuingInstitutionName;
    }

    public void setIssuingInstitutionName(String issuingInstitutionName) {
        this.issuingInstitutionName = issuingInstitutionName;
    }

    public InstitutionTrustLevel getInstitutionTrustLevel() {
        return institutionTrustLevel;
    }

    public void setInstitutionTrustLevel(InstitutionTrustLevel institutionTrustLevel) {
        this.institutionTrustLevel = institutionTrustLevel;
    }

    public boolean isInstitutionVerified() {
        return institutionVerified;
    }

    public void setInstitutionVerified(boolean institutionVerified) {
        this.institutionVerified = institutionVerified;
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

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public TrustLevel getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(TrustLevel trustScore) {
        this.trustScore = trustScore;
    }
}
