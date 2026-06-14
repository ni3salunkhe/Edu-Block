package com.edu.dto;

public class InstitutionTrustUpdateRequest {
    private String reason;

    public InstitutionTrustUpdateRequest() {}

    public InstitutionTrustUpdateRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
