package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

public class DocumentIntelligenceDTO {
    
    private UUID documentId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private boolean processed;
    private String classification;
    private String studentName;
    private String institutionName;
    private String enrollmentNumber;
    private String degreeProgram;
    private Integer graduationYear;
    private String academicScores;
    private String credentialReference;
    private Instant issueDate;
    private Instant processedAt;
    private String errorMessage;

    public DocumentIntelligenceDTO() {
    }

    public DocumentIntelligenceDTO(UUID documentId, String status, boolean processed, String errorMessage) {
        this.documentId = documentId;
        this.status = status;
        this.processed = processed;
        this.errorMessage = errorMessage;
    }

    public DocumentIntelligenceDTO(UUID documentId, String status, boolean processed, String classification, 
                                   String studentName, String institutionName, String enrollmentNumber, 
                                   String degreeProgram, Integer graduationYear, String academicScores, 
                                   String credentialReference, Instant issueDate, Instant processedAt, 
                                   String errorMessage) {
        this.documentId = documentId;
        this.status = status;
        this.processed = processed;
        this.classification = classification;
        this.studentName = studentName;
        this.institutionName = institutionName;
        this.enrollmentNumber = enrollmentNumber;
        this.degreeProgram = degreeProgram;
        this.graduationYear = graduationYear;
        this.academicScores = academicScores;
        this.credentialReference = credentialReference;
        this.issueDate = issueDate;
        this.processedAt = processedAt;
        this.errorMessage = errorMessage;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
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

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
