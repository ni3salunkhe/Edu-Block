package com.edu.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
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

    public enum Status {
        UPLOADED,
        AI_FLAGGED,
        AI_VERIFIED,
        APPROVED,
        REJECTED,
        BLOCKCHAIN_ANCHORED
    }


}
