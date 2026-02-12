package com.edu.dto;

import java.util.Objects;
import java.util.UUID;

public class VerifierDocumentDTO {
    private UUID id;
    private String fileName;
    private String ownerName;
    private String status;
    private String aiStatus; // e.g., "Non-Original" or "Unique" or AI score description
    private String sha256Hash;
    private String blockchainHash;
    private String previewUrl;

    public VerifierDocumentDTO() {
    }

    public VerifierDocumentDTO(UUID id, String fileName, String ownerName, String status, 
                               String aiStatus, String sha256Hash, String blockchainHash, String previewUrl) {
        this.id = id;
        this.fileName = fileName;
        this.ownerName = ownerName;
        this.status = status;
        this.aiStatus = aiStatus;
        this.sha256Hash = sha256Hash;
        this.blockchainHash = blockchainHash;
        this.previewUrl = previewUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public String getBlockchainHash() {
        return blockchainHash;
    }

    public void setBlockchainHash(String blockchainHash) {
        this.blockchainHash = blockchainHash;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerifierDocumentDTO that = (VerifierDocumentDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(fileName, that.fileName) &&
               Objects.equals(ownerName, that.ownerName) &&
               Objects.equals(status, that.status) &&
               Objects.equals(aiStatus, that.aiStatus) &&
               Objects.equals(sha256Hash, that.sha256Hash) &&
               Objects.equals(blockchainHash, that.blockchainHash) &&
               Objects.equals(previewUrl, that.previewUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, ownerName, status, aiStatus, sha256Hash, blockchainHash, previewUrl);
    }

    @Override
    public String toString() {
        return "VerifierDocumentDTO{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", status='" + status + '\'' +
                ", aiStatus='" + aiStatus + '\'' +
                ", sha256Hash='" + sha256Hash + '\'' +
                ", blockchainHash='" + blockchainHash + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
                '}';
    }
}
