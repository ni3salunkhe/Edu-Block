package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_intelligence", indexes = {
    @Index(name = "idx_doc_intel_student", columnList = "studentName"),
    @Index(name = "idx_doc_intel_inst", columnList = "institutionName"),
    @Index(name = "idx_doc_intel_degree", columnList = "degreeProgram"),
    @Index(name = "idx_doc_intel_status", columnList = "processingStatus")
})
public class DocumentIntelligence {

    @Id
    private UUID documentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "document_id")
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IntelligenceStatus processingStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DocumentClassification classification;

    private String studentName;
    private String institutionName;
    private String enrollmentNumber;
    private String degreeProgram;
    private Integer graduationYear;
    private String academicScores;
    private String credentialReference;
    private Instant issueDate;

    // Similarity Audit Trail Columns
    private UUID matchedDocumentId;
    private Double similarityScore;

    @Column(columnDefinition = "TEXT")
    private String auditMetadataJson;

    private Instant processedAt;

    public enum IntelligenceStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public enum DocumentClassification {
        DEGREE_CERTIFICATE,
        TRANSCRIPT,
        MARKSHEET,
        ID_CARD,
        UNKNOWN
    }

    public DocumentIntelligence() {
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

    public IntelligenceStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(IntelligenceStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public DocumentClassification getClassification() {
        return classification;
    }

    public void setClassification(DocumentClassification classification) {
        this.classification = classification;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getDegreeProgram() {
        return degreeProgram;
    }

    public void setDegreeProgram(String degreeProgram) {
        this.degreeProgram = degreeProgram;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getAcademicScores() {
        return academicScores;
    }

    public void setAcademicScores(String academicScores) {
        this.academicScores = academicScores;
    }

    public String getCredentialReference() {
        return credentialReference;
    }

    public void setCredentialReference(String credentialReference) {
        this.credentialReference = credentialReference;
    }

    public Instant getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Instant issueDate) {
        this.issueDate = issueDate;
    }

    public UUID getMatchedDocumentId() {
        return matchedDocumentId;
    }

    public void setMatchedDocumentId(UUID matchedDocumentId) {
        this.matchedDocumentId = matchedDocumentId;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getAuditMetadataJson() {
        return auditMetadataJson;
    }

    public void setAuditMetadataJson(String auditMetadataJson) {
        this.auditMetadataJson = auditMetadataJson;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
