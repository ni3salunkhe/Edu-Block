package com.edu.config;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class MfaTokenStore {

    private final Map<String, UUID> tokenToUser = new ConcurrentHashMap<>();
    private final Map<String, Instant> expiry = new ConcurrentHashMap<>();

    public String createToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        tokenToUser.put(token, userId);
        expiry.put(token, Instant.now().plusSeconds(120)); // 2 min
        return token;
    }

    public UUID consumeToken(String token) {
        Instant exp = expiry.get(token);
        if (exp == null || Instant.now().isAfter(exp)) {
            throw new RuntimeException("MFA token expired");
        }
        expiry.remove(token);
        return tokenToUser.remove(token);
    }
}
