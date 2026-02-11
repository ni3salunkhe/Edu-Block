package com.edu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.entity.EmployerConsent;
import com.edu.repositories.EmployerConsentRepository;
import com.edu.services.AuditService;
import com.edu.services.ConsentService;
import com.edu.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/individual/consents")
@PreAuthorize("hasRole('INDIVIDUAL')")
public class IndividualConsentController {

    private final EmployerConsentRepository consentRepo;
    private final ConsentService consentService;
    private final AuditService auditService;

    public IndividualConsentController(EmployerConsentRepository consentRepo,
                                       ConsentService consentService,
                                       AuditService auditService) {
        this.consentRepo = consentRepo;
        this.consentService = consentService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<com.edu.dto.ConsentViewModel> getConsents() {
        UUID ownerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(consentService.getConsents(ownerId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveConsent(@PathVariable UUID id, HttpServletRequest request) {
        UUID ownerId = SecurityUtil.getCurrentUserId();
        consentService.approveConsentById(id, ownerId);
        auditService.logIndividualAction(ownerId, "APPROVE_CONSENT", "CONSENT", id, null, request.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectConsent(@PathVariable UUID id, HttpServletRequest request) {
        UUID ownerId = SecurityUtil.getCurrentUserId();
        consentService.rejectConsentById(id, ownerId);
        auditService.logIndividualAction(ownerId, "REJECT_CONSENT", "CONSENT", id, null, request.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<Void> revokeConsent(@PathVariable UUID id, HttpServletRequest request) {
        UUID ownerId = SecurityUtil.getCurrentUserId();
        consentService.revokeConsentById(id, ownerId);
        auditService.logIndividualAction(ownerId, "REVOKE_CONSENT", "CONSENT", id, null, request.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}

