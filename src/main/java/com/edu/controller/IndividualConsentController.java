package com.edu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/pending")
    public ResponseEntity<List<EmployerConsent>> pendingConsents() {

        UUID ownerId = SecurityUtil.getCurrentUserId();

        return ResponseEntity.ok(
            consentRepo.findByOwnerIdAndStatus(
                ownerId,
                EmployerConsent.Status.REQUESTED
            )
        );
    }
    @PostMapping("/approve")
    public ResponseEntity<Void> approveConsent(
            @RequestParam UUID employerId,
            HttpServletRequest request) {

        UUID ownerId = SecurityUtil.getCurrentUserId();

        consentService.approveConsent(ownerId, employerId);

        auditService.logIndividualAction(
            ownerId,
            "APPROVE_CONSENT",
            "USER",
            employerId,
            null,
            request.getRemoteAddr()
        );

        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/revoke")
    public ResponseEntity<Void> revokeConsent(
            @RequestParam UUID employerId,
            HttpServletRequest request) {

        UUID ownerId = SecurityUtil.getCurrentUserId();

        consentService.revokeConsent(ownerId, employerId);

        auditService.logIndividualAction(
            ownerId,
            "REVOKE_CONSENT",
            "USER",
            employerId,
            null,
            request.getRemoteAddr()
        );

        return ResponseEntity.ok().build();
    }
}

