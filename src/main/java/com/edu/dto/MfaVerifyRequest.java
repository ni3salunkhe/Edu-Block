package com.edu.dto;

import java.util.Objects;

public class MfaVerifyRequest {
    private String mfaToken;
    private int otp;

    public String getMfaToken() {
        return mfaToken;
    }

    public void setMfaToken(String mfaToken) {
        this.mfaToken = mfaToken;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MfaVerifyRequest that = (MfaVerifyRequest) o;
        return otp == that.otp && Objects.equals(mfaToken, that.mfaToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mfaToken, otp);
    }

    @Override
    public String toString() {
        return "MfaVerifyRequest{" +
                "mfaToken='" + mfaToken + '\'' +
                ", otp=" + otp +
                '}';
    }
}
