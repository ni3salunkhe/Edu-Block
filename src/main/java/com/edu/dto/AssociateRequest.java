package com.edu.dto;

public class AssociateRequest {
    private String enrollmentNumber;

    public AssociateRequest() {}

    public AssociateRequest(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }
}
