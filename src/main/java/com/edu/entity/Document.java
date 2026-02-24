package com.edu.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "documents", uniqueConstraints = {
    @UniqueConstraint(columnNames = "sha256Hash")
})

public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID ownerId;

    private String documentType;

    private String filePath;

    @Column(nullable = false)
    private String sha256Hash;

    @Column(length = 1024)
    private String perceptualHash;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String blockchainTx;

    private Instant createdAt;

    private UUID matchedDocumentId;

    @Column(length = 1000)
    private String rejectionComment;

    public enum Status {
        UPLOADED,
        AI_FLAGGED,
        AI_VERIFIED,
        APPROVED,
        REJECTED,
        BLOCKCHAIN_ANCHORED
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public String getPerceptualHash() {
        return perceptualHash;
    }

    public void setPerceptualHash(String perceptualHash) {
        this.perceptualHash = perceptualHash;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getBlockchainTx() {
        return blockchainTx;
    }

    public void setBlockchainTx(String blockchainTx) {
        this.blockchainTx = blockchainTx;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getMatchedDocumentId() {
        return matchedDocumentId;
    }

    public void setMatchedDocumentId(UUID matchedDocumentId) {
        this.matchedDocumentId = matchedDocumentId;
    }

    public String getRejectionComment() {
        return rejectionComment;
    }

    public void setRejectionComment(String rejectionComment) {
        this.rejectionComment = rejectionComment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) &&
               Objects.equals(ownerId, document.ownerId) &&
               Objects.equals(documentType, document.documentType) &&
               Objects.equals(filePath, document.filePath) &&
               Objects.equals(sha256Hash, document.sha256Hash) &&
               Objects.equals(perceptualHash, document.perceptualHash) &&
               status == document.status &&
               Objects.equals(blockchainTx, document.blockchainTx) &&
               Objects.equals(createdAt, document.createdAt) &&
               Objects.equals(matchedDocumentId, document.matchedDocumentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, documentType, filePath, sha256Hash, 
                          perceptualHash, status, blockchainTx, createdAt, matchedDocumentId);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", documentType='" + documentType + '\'' +
                ", filePath='" + filePath + '\'' +
                ", sha256Hash='" + sha256Hash + '\'' +
                ", status=" + status +
                ", blockchainTx='" + blockchainTx + '\'' +
                ", createdAt=" + createdAt +
                ", matchedDocumentId=" + matchedDocumentId +
                '}';
    }

}
