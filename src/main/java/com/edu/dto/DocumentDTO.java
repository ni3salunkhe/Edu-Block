package com.edu.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.edu.entity.Document;

public class DocumentDTO {
    private UUID id;
    private String fileName;
    private Document.Status status;
    private Instant createdAt;
    private String filePath;

    public DocumentDTO() {
    }

    public DocumentDTO(UUID id, String fileName, Document.Status status, Instant createdAt, String filePath) {
        this.id = id;
        this.fileName = fileName;
        this.status = status;
        this.createdAt = createdAt;
        this.filePath = filePath;
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

    public Document.Status getStatus() {
        return status;
    }

    public void setStatus(Document.Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentDTO that = (DocumentDTO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(fileName, that.fileName) &&
               status == that.status &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(filePath, that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, status, createdAt, filePath);
    }

    @Override
    public String toString() {
        return "DocumentDTO{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
