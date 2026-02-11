package com.edu.services;

import com.edu.dto.LoginRequest;
import com.edu.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
