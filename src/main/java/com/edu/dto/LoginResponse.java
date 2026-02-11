package com.edu.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;       // JWT (normal users)
    private String role;
    private boolean mfaRequired;
    private String mfaToken;    // admin only
}
