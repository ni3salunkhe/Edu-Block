package com.edu.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "document_hashes", indexes = {
    @Index(name = "idx_doc_hash_c0", columnList = "chunk0"),
    @Index(name = "idx_doc_hash_c1", columnList = "chunk1"),
    @Index(name = "idx_doc_hash_c2", columnList = "chunk2"),
    @Index(name = "idx_doc_hash_c3", columnList = "chunk3"),
    @Index(name = "idx_doc_hash_c4", columnList = "chunk4"),
    @Index(name = "idx_doc_hash_c5", columnList = "chunk5"),
    @Index(name = "idx_doc_hash_c6", columnList = "chunk6"),
    @Index(name = "idx_doc_hash_c7", columnList = "chunk7")
})
public class DocumentHash {

    @Id
    private UUID documentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(length = 128, nullable = false)
    private String chunk0;

    @Column(length = 128, nullable = false)
    private String chunk1;

    @Column(length = 128, nullable = false)
    private String chunk2;

    @Column(length = 128, nullable = false)
    private String chunk3;

    @Column(length = 128, nullable = false)
    private String chunk4;

    @Column(length = 128, nullable = false)
    private String chunk5;

    @Column(length = 128, nullable = false)
    private String chunk6;

    @Column(length = 128, nullable = false)
    private String chunk7;

    @Column(length = 1024, nullable = false)
    private String fullHash;

    public DocumentHash() {
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getChunk0() {
        return chunk0;
    }

    public void setChunk0(String chunk0) {
        this.chunk0 = chunk0;
    }

    public String getChunk1() {
        return chunk1;
    }

    public void setChunk1(String chunk1) {
        this.chunk1 = chunk1;
    }

    public String getChunk2() {
        return chunk2;
    }

    public void setChunk2(String chunk2) {
        this.chunk2 = chunk2;
    }

    public String getChunk3() {
        return chunk3;
    }

    public void setChunk3(String chunk3) {
        this.chunk3 = chunk3;
    }

    public String getChunk4() {
        return chunk4;
    }

    public void setChunk4(String chunk4) {
        this.chunk4 = chunk4;
    }

    public String getChunk5() {
        return chunk5;
    }

    public void setChunk5(String chunk5) {
        this.chunk5 = chunk5;
    }

    public String getChunk6() {
        return chunk6;
    }

    public void setChunk6(String chunk6) {
        this.chunk6 = chunk6;
    }

    public String getChunk7() {
        return chunk7;
    }

    public void setChunk7(String chunk7) {
        this.chunk7 = chunk7;
    }

    public String getFullHash() {
        return fullHash;
    }

    public void setFullHash(String fullHash) {
        this.fullHash = fullHash;
    }
}
