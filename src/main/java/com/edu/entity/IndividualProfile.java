package com.edu.entity;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "individual_profiles")
public class IndividualProfile {

    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String fullName;
    private LocalDate dob;
    private String phone;
    private String address;

    private boolean institutionIssued; // true if created by institution

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isInstitutionIssued() {
        return institutionIssued;
    }

    public void setInstitutionIssued(boolean institutionIssued) {
        this.institutionIssued = institutionIssued;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndividualProfile that = (IndividualProfile) o;
        return institutionIssued == that.institutionIssued &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(fullName, that.fullName) &&
               Objects.equals(dob, that.dob) &&
               Objects.equals(phone, that.phone) &&
               Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, fullName, dob, phone, address, institutionIssued);
    }

    @Override
    public String toString() {
        return "IndividualProfile{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", dob=" + dob +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", institutionIssued=" + institutionIssued +
                '}';
    }

}
