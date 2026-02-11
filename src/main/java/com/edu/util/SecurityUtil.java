package com.edu.util;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static UUID getCurrentUserId() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated access");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof String)) {
            throw new RuntimeException("Invalid authentication principal");
        }

        try {
            return UUID.fromString((String) principal);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid userId in security context");
        }
    }

    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated access");
        }

        return auth.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .filter(role -> role.startsWith("ROLE_"))
            .map(role -> role.substring(5))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User has no role"));
    }
}
