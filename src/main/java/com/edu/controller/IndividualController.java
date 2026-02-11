package com.edu.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.IndividualSummaryDTO;
import com.edu.entity.EmployerConsent;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.EmployerConsentRepository;
import com.edu.util.SecurityUtil;

@RestController
@RequestMapping("/api/individual")
@PreAuthorize("hasRole('INDIVIDUAL')")
public class IndividualController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EmployerConsentRepository consentRepository;

    @GetMapping("/summary")
    public IndividualSummaryDTO getSummary() {
        UUID userId = SecurityUtil.getCurrentUserId();

        long totalDocuments = documentRepository.countByOwnerId(userId);
        long pendingConsents = consentRepository.countByOwnerIdAndStatus(userId, EmployerConsent.Status.REQUESTED);
        long activeEmployers = consentRepository.countByOwnerIdAndStatus(userId, EmployerConsent.Status.APPROVED);

        return new IndividualSummaryDTO(totalDocuments, pendingConsents, activeEmployers);
    }
}
