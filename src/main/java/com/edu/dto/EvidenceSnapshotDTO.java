package com.edu.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class EvidenceSnapshotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String snapshotVersion; // "SNAPSHOT_V1" - to track evolving schemas
    private String ocrTextHash;
    private double ocrWordConfidence;
    private UUID resolvedInstitutionId;
    private String institutionName;
    private int institutionTrustScore;
    private String institutionTrustTier;
    private boolean hasActiveRiskEvents;
    private List<String> activeFraudSignals; // List of signal types + severity
    private List<String> verificationExplanations;
    private boolean hasDuplicateSimilarity;
    private boolean blockchainAnchored;
    private String ownerName;

    public EvidenceSnapshotDTO() {}

    public String getSnapshotVersion() {
        return snapshotVersion;
    }

    public void setSnapshotVersion(String snapshotVersion) {
        this.snapshotVersion = snapshotVersion;
    }

    public String getOcrTextHash() {
        return ocrTextHash;
    }

    public void setOcrTextHash(String ocrTextHash) {
        this.ocrTextHash = ocrTextHash;
    }

    public double getOcrWordConfidence() {
        return ocrWordConfidence;
    }

    public void setOcrWordConfidence(double ocrWordConfidence) {
        this.ocrWordConfidence = ocrWordConfidence;
    }

    public UUID getResolvedInstitutionId() {
        return resolvedInstitutionId;
    }

    public void setResolvedInstitutionId(UUID resolvedInstitutionId) {
        this.resolvedInstitutionId = resolvedInstitutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public int getInstitutionTrustScore() {
        return institutionTrustScore;
    }

    public void setInstitutionTrustScore(int institutionTrustScore) {
        this.institutionTrustScore = institutionTrustScore;
    }

    public String getInstitutionTrustTier() {
        return institutionTrustTier;
    }

    public void setInstitutionTrustTier(String institutionTrustTier) {
        this.institutionTrustTier = institutionTrustTier;
    }

    public boolean isHasActiveRiskEvents() {
        return hasActiveRiskEvents;
    }

    public void setHasActiveRiskEvents(boolean hasActiveRiskEvents) {
        this.hasActiveRiskEvents = hasActiveRiskEvents;
    }

    public List<String> getActiveFraudSignals() {
        return activeFraudSignals;
    }

    public void setActiveFraudSignals(List<String> activeFraudSignals) {
        this.activeFraudSignals = activeFraudSignals;
    }

    public List<String> getVerificationExplanations() {
        return verificationExplanations;
    }

    public void setVerificationExplanations(List<String> verificationExplanations) {
        this.verificationExplanations = verificationExplanations;
    }

    public boolean isHasDuplicateSimilarity() {
        return hasDuplicateSimilarity;
    }

    public void setHasDuplicateSimilarity(boolean hasDuplicateSimilarity) {
        this.hasDuplicateSimilarity = hasDuplicateSimilarity;
    }

    public boolean isBlockchainAnchored() {
        return blockchainAnchored;
    }

    public void setBlockchainAnchored(boolean blockchainAnchored) {
        this.blockchainAnchored = blockchainAnchored;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
