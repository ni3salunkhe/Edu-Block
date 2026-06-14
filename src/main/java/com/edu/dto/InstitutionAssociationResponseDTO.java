package com.edu.dto;

import java.time.Instant;
import java.util.UUID;

import com.edu.entity.AssociationStatus;

public class InstitutionAssociationResponseDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private UUID institutionId;
    private String institutionName;
    private String enrollmentNumber;
    private AssociationStatus status;
    private UUID reviewedBy;
    private String reviewReason;
    private Instant requestedAt;
    private Instant reviewedAt;

    public InstitutionAssociationResponseDTO() {}

    public InstitutionAssociationResponseDTO(UUID id, UUID studentId, String studentName, String studentEmail,
                                             UUID institutionId, String institutionName, String enrollmentNumber,
                                             AssociationStatus status, UUID reviewedBy, String reviewReason,
                                             Instant requestedAt, Instant reviewedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.enrollmentNumber = enrollmentNumber;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewReason = reviewReason;
        this.requestedAt = requestedAt;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(UUID institutionId) {
        this.institutionId = institutionId;
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

    public AssociationStatus getStatus() {
        return status;
    }

    public void setStatus(AssociationStatus status) {
        this.status = status;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public void setReviewReason(String reviewReason) {
        this.reviewReason = reviewReason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
