package com.edu.dto;

import lombok.Data;

@Data
public class MfaVerifyRequest {
    private String mfaToken;
    private int otp;
}
