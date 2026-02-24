package com.edu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple per-IP token-bucket rate limiter.
 *
 * Limits (per IP, per minute):
 *   /api/auth/login        → 5 requests
 *   /api/documents/upload  → 10 requests
 *   All other endpoints    → 60 requests (general guard)
 *
 * Buckets refill every 60 seconds by a background thread.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // --- Config -------------------------------------------------------
    private static final int LOGIN_LIMIT   = 5;
    private static final int UPLOAD_LIMIT  = 10;
    private static final int DEFAULT_LIMIT = 60;
    private static final long REFILL_MS    = 60_000L; // 1 minute

    // --- Buckets: ip -> remaining tokens -----------------------------
    private final Map<String, AtomicInteger> loginBuckets   = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> uploadBuckets  = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> defaultBuckets = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        // Background thread refills all buckets every minute
        Thread refiller = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(REFILL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                loginBuckets.values().forEach(c  -> c.set(LOGIN_LIMIT));
                uploadBuckets.values().forEach(c -> c.set(UPLOAD_LIMIT));
                defaultBuckets.values().forEach(c -> c.set(DEFAULT_LIMIT));
            }
        });
        refiller.setDaemon(true);
        refiller.setName("rate-limit-refiller");
        refiller.start();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String ip   = resolveClientIp(request);
        String path = request.getServletPath();

        if (!tryConsume(ip, path)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\"," +
                "\"message\":\"Rate limit exceeded. Please try again in 60 seconds.\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean tryConsume(String ip, String path) {
        if (path.contains("/api/auth/login")) {
            return loginBuckets
                .computeIfAbsent(ip, k -> new AtomicInteger(LOGIN_LIMIT))
                .getAndDecrement() > 0;
        }
        if (path.contains("/api/documents/upload")) {
            return uploadBuckets
                .computeIfAbsent(ip, k -> new AtomicInteger(UPLOAD_LIMIT))
                .getAndDecrement() > 0;
        }
        return defaultBuckets
            .computeIfAbsent(ip, k -> new AtomicInteger(DEFAULT_LIMIT))
            .getAndDecrement() > 0;
    }

    /** Respects X-Forwarded-For so Render's reverse proxy is handled correctly. */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
