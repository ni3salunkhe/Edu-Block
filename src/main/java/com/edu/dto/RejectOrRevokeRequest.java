package com.edu.dto;

public class RejectOrRevokeRequest {
    private String reason;

    public RejectOrRevokeRequest() {}

    public RejectOrRevokeRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
