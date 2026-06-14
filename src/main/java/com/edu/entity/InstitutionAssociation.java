package com.edu.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "institution_associations")
public class InstitutionAssociation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    private String enrollmentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssociationStatus status;

    private UUID reviewedBy;

    @Column(length = 1000)
    private String reviewReason;

    @Column(nullable = false)
    private Instant requestedAt;

    private Instant reviewedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = Instant.now();
        if (status == null) {
            status = AssociationStatus.PENDING;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionAssociation that = (InstitutionAssociation) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(student, that.student) &&
               Objects.equals(institution, that.institution) &&
               Objects.equals(enrollmentNumber, that.enrollmentNumber) &&
               status == that.status &&
               Objects.equals(reviewedBy, that.reviewedBy) &&
               Objects.equals(reviewReason, that.reviewReason) &&
               Objects.equals(requestedAt, that.requestedAt) &&
               Objects.equals(reviewedAt, that.reviewedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, student, institution, enrollmentNumber, status, reviewedBy, reviewReason, requestedAt, reviewedAt);
    }

    @Override
    public String toString() {
        return "InstitutionAssociation{" +
                "id=" + id +
                ", enrollmentNumber='" + enrollmentNumber + '\'' +
                ", status=" + status +
                ", reviewedBy=" + reviewedBy +
                ", requestedAt=" + requestedAt +
                '}';
    }
}
