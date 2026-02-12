package com.edu.dto;

import java.util.Objects;

public class IndividualSummaryDTO {
    private long totalDocuments;
    private long pendingConsents;
    private long activeEmployers;

    public IndividualSummaryDTO() {
    }

    public IndividualSummaryDTO(long totalDocuments, long pendingConsents, long activeEmployers) {
        this.totalDocuments = totalDocuments;
        this.pendingConsents = pendingConsents;
        this.activeEmployers = activeEmployers;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getPendingConsents() {
        return pendingConsents;
    }

    public void setPendingConsents(long pendingConsents) {
        this.pendingConsents = pendingConsents;
    }

    public long getActiveEmployers() {
        return activeEmployers;
    }

    public void setActiveEmployers(long activeEmployers) {
        this.activeEmployers = activeEmployers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndividualSummaryDTO that = (IndividualSummaryDTO) o;
        return totalDocuments == that.totalDocuments &&
               pendingConsents == that.pendingConsents &&
               activeEmployers == that.activeEmployers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalDocuments, pendingConsents, activeEmployers);
    }

    @Override
    public String toString() {
        return "IndividualSummaryDTO{" +
                "totalDocuments=" + totalDocuments +
                ", pendingConsents=" + pendingConsents +
                ", activeEmployers=" + activeEmployers +
                '}';
    }
}
