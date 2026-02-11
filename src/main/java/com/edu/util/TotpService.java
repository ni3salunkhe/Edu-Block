package com.edu.util;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class TotpService {

    private final GoogleAuthenticator authenticator =
            new GoogleAuthenticator();

    public GoogleAuthenticatorKey generateSecret() {
        return authenticator.createCredentials();
    }

    public boolean verifyCode(String secret, int code) {
        return authenticator.authorize(secret, code);
    }
}
