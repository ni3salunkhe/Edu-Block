package com.edu.dto;

import java.util.Objects;

public class LoginResponse {

    private String token;       // JWT (normal users)
    private String role;
    private boolean mfaRequired;
    private boolean mfaSetupRequired;
    private String mfaToken;    // admin only

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isMfaRequired() {
        return mfaRequired;
    }

    public void setMfaRequired(boolean mfaRequired) {
        this.mfaRequired = mfaRequired;
    }

    public boolean isMfaSetupRequired() {
        return mfaSetupRequired;
    }

    public void setMfaSetupRequired(boolean mfaSetupRequired) {
        this.mfaSetupRequired = mfaSetupRequired;
    }

    public String getMfaToken() {
        return mfaToken;
    }

    public void setMfaToken(String mfaToken) {
        this.mfaToken = mfaToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse that = (LoginResponse) o;
        return mfaRequired == that.mfaRequired &&
               mfaSetupRequired == that.mfaSetupRequired &&
               Objects.equals(token, that.token) &&
               Objects.equals(role, that.role) &&
               Objects.equals(mfaToken, that.mfaToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, role, mfaRequired, mfaSetupRequired, mfaToken);
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + token + '\'' +
                ", role='" + role + '\'' +
                ", mfaRequired=" + mfaRequired +
                ", mfaSetupRequired=" + mfaSetupRequired +
                ", mfaToken='" + mfaToken + '\'' +
                '}';
    }
}
