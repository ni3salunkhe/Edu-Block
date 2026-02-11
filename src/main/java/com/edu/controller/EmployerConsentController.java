package com.edu.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.services.AuditService;
import com.edu.services.ConsentService;
import com.edu.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/employer/consents")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerConsentController {

    private final ConsentService consentService;
    private final AuditService auditService;

    public EmployerConsentController(ConsentService consentService,
                                     AuditService auditService) {
        this.consentService = consentService;
        this.auditService = auditService;
    }

    @PostMapping("/request")
    public ResponseEntity<Void> requestConsent(
            @RequestParam UUID ownerId,
            HttpServletRequest request) {

        UUID employerId = SecurityUtil.getCurrentUserId();

        consentService.requestConsent(employerId, ownerId);

        auditService.logEmployerAction(
            employerId,
            "REQUEST_CONSENT",
            "USER",
            ownerId,
            null,
            request.getRemoteAddr()
        );

        return ResponseEntity.accepted().build();
    }
}
