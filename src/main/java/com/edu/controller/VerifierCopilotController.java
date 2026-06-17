package com.edu.controller;

import com.edu.entity.ReviewerFeedback;
import com.edu.entity.ReviewerRecommendation;
import com.edu.services.ReviewerCopilotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/verifier/document/{id}")
@PreAuthorize("hasRole('VERIFIER')")
public class VerifierCopilotController {

    private final ReviewerCopilotService copilotService;

    public VerifierCopilotController(ReviewerCopilotService copilotService) {
        this.copilotService = copilotService;
    }

    @GetMapping("/recommendation")
    public ResponseEntity<ReviewerRecommendation> getRecommendation(@PathVariable("id") UUID documentId) {
        ReviewerRecommendation rec = copilotService.getActiveRecommendation(documentId);
        if (rec == null) {
            // Generate it dynamically if it hasn't been pre-generated
            rec = copilotService.generateRecommendation(documentId);
        }
        // (B) Record first-view timestamp for workload analytics
        copilotService.markOpened(documentId);
        return ResponseEntity.ok(rec);
    }

    @PostMapping("/recommendation/refresh")
    public ResponseEntity<?> refreshRecommendation(@PathVariable("id") UUID documentId) {
        try {
            ReviewerRecommendation rec = copilotService.refreshRecommendation(documentId);
            return ResponseEntity.ok(rec);
        } catch (IllegalStateException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(err);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable("id") UUID documentId, @RequestBody Map<String, String> body) {
        ReviewerRecommendation rec = copilotService.getActiveRecommendation(documentId);
        if (rec == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No active recommendation found for this document"));
        }

        UUID verifierId = getAuthenticatedVerifierId();
        String actualDecision = body.get("actualDecision");
        String comment = body.get("comment");
        String overrideReason = body.get("overrideReason");

        try {
            ReviewerFeedback feedback = copilotService.saveFeedback(rec.getId(), verifierId, actualDecision, comment, overrideReason);
            // (B) Record completion timestamp for workload analytics
            copilotService.markCompleted(documentId);
            return ResponseEntity.ok(feedback);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    private UUID getAuthenticatedVerifierId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
            SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
            throw new AccessDeniedException("User must be authenticated");
        }
        try {
            return UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
            );
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid authenticated user identity");
        }
    }
}
