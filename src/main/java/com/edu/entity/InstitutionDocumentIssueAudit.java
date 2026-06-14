package com.edu.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "institution_document_issue_audits")
public class InstitutionDocumentIssueAudit {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private UUID institutionId;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID issuedBy;

    @Column(nullable = false)
    private String credentialReference;

    @Column(nullable = false)
    private Instant issuedAt;

    @PrePersist
    protected void onCreate() {
        issuedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(UUID issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getCredentialReference() {
        return credentialReference;
    }

    public void setCredentialReference(String credentialReference) {
        this.credentialReference = credentialReference;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionDocumentIssueAudit that = (InstitutionDocumentIssueAudit) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(documentId, that.documentId) &&
               Objects.equals(institutionId, that.institutionId) &&
               Objects.equals(studentId, that.studentId) &&
               Objects.equals(issuedBy, that.issuedBy) &&
               Objects.equals(credentialReference, that.credentialReference) &&
               Objects.equals(issuedAt, that.issuedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentId, institutionId, studentId, issuedBy, credentialReference, issuedAt);
    }

    @Override
    public String toString() {
        return "InstitutionDocumentIssueAudit{" +
                "id=" + id +
                ", documentId=" + documentId +
                ", institutionId=" + institutionId +
                ", studentId=" + studentId +
                ", issuedBy=" + issuedBy +
                ", credentialReference='" + credentialReference + '\'' +
                ", issuedAt=" + issuedAt +
                '}';
    }
}
