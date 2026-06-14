package com.edu.dto;

import com.edu.entity.DocumentSource;
import com.edu.entity.InstitutionTrustLevel;
import java.util.List;
import java.util.UUID;

public class EmployerDocumentViewDTO {
    private UUID documentId;
    private String documentName;
    private String documentType;
    private String ownerName;
    private String ownerEmail;
    private TrustLevel trustScore;
    private TrustSource trustSource;
    private DocumentSource documentSource;
    private String issuingInstitutionName;
    private InstitutionTrustLevel institutionTrustLevel;
    private boolean institutionVerified;
    private String credentialReference;
    private boolean blockchainAnchored;
    private String transactionHash;
    private String verificationExplanation;
    private boolean consentGranted;
    private List<TrustBadgeType> trustBadges;

    public EmployerDocumentViewDTO() {}

    public EmployerDocumentViewDTO(UUID documentId, String documentName, String documentType,
                                   String ownerName, String ownerEmail, TrustLevel trustScore,
                                   TrustSource trustSource, DocumentSource documentSource,
                                   String issuingInstitutionName, InstitutionTrustLevel institutionTrustLevel,
                                   boolean institutionVerified, String credentialReference,
                                   boolean blockchainAnchored, String transactionHash,
                                   String verificationExplanation, boolean consentGranted,
                                   List<TrustBadgeType> trustBadges) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.documentType = documentType;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.trustScore = trustScore;
        this.trustSource = trustSource;
        this.documentSource = documentSource;
        this.issuingInstitutionName = issuingInstitutionName;
        this.institutionTrustLevel = institutionTrustLevel;
        this.institutionVerified = institutionVerified;
        this.credentialReference = credentialReference;
        this.blockchainAnchored = blockchainAnchored;
        this.transactionHash = transactionHash;
        this.verificationExplanation = verificationExplanation;
        this.consentGranted = consentGranted;
        this.trustBadges = trustBadges;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public TrustLevel getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(TrustLevel trustScore) {
        this.trustScore = trustScore;
    }

    public TrustSource getTrustSource() {
        return trustSource;
    }

    public void setTrustSource(TrustSource trustSource) {
        this.trustSource = trustSource;
    }

    public DocumentSource getDocumentSource() {
        return documentSource;
    }

    public void setDocumentSource(DocumentSource documentSource) {
        this.documentSource = documentSource;
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

    public String getVerificationExplanation() {
        return verificationExplanation;
    }

    public void setVerificationExplanation(String verificationExplanation) {
        this.verificationExplanation = verificationExplanation;
    }

    public boolean isConsentGranted() {
        return consentGranted;
    }

    public void setConsentGranted(boolean consentGranted) {
        this.consentGranted = consentGranted;
    }

    public List<TrustBadgeType> getTrustBadges() {
        return trustBadges;
    }

    public void setTrustBadges(List<TrustBadgeType> trustBadges) {
        this.trustBadges = trustBadges;
    }
}
