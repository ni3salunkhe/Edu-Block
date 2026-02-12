package com.edu.dto;

import java.util.Objects;

public class AdminSummaryDTO {
    private long totalUsers;
    private long verifiers;
    private long activeConsents;

    public AdminSummaryDTO() {
    }

    public AdminSummaryDTO(long totalUsers, long verifiers, long activeConsents) {
        this.totalUsers = totalUsers;
        this.verifiers = verifiers;
        this.activeConsents = activeConsents;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getVerifiers() {
        return verifiers;
    }

    public void setVerifiers(long verifiers) {
        this.verifiers = verifiers;
    }

    public long getActiveConsents() {
        return activeConsents;
    }

    public void setActiveConsents(long activeConsents) {
        this.activeConsents = activeConsents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminSummaryDTO that = (AdminSummaryDTO) o;
        return totalUsers == that.totalUsers &&
               verifiers == that.verifiers &&
               activeConsents == that.activeConsents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalUsers, verifiers, activeConsents);
    }

    @Override
    public String toString() {
        return "AdminSummaryDTO{" +
                "totalUsers=" + totalUsers +
                ", verifiers=" + verifiers +
                ", activeConsents=" + activeConsents +
                '}';
    }
}
