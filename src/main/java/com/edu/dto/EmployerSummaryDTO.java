package com.edu.dto;

import java.util.Objects;

public class EmployerSummaryDTO {
    private long pending;
    private long approved;
    private long rejected;

    public EmployerSummaryDTO() {
    }

    public EmployerSummaryDTO(long pending, long approved, long rejected) {
        this.pending = pending;
        this.approved = approved;
        this.rejected = rejected;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getApproved() {
        return approved;
    }

    public void setApproved(long approved) {
        this.approved = approved;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployerSummaryDTO that = (EmployerSummaryDTO) o;
        return pending == that.pending &&
               approved == that.approved &&
               rejected == that.rejected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pending, approved, rejected);
    }

    @Override
    public String toString() {
        return "EmployerSummaryDTO{" +
                "pending=" + pending +
                ", approved=" + approved +
                ", rejected=" + rejected +
                '}';
    }
}
