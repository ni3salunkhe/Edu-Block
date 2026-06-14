package com.edu.servicesImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.edu.dto.ConsentDTO;
import com.edu.dto.ConsentViewModel;
import com.edu.entity.EmployerConsent;
import com.edu.entity.EmployerProfile;
import com.edu.entity.User;
import com.edu.repositories.EmployerConsentRepository;
import com.edu.repositories.EmployerProfileRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.ConsentService;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsentServiceImpl implements ConsentService {

    private final EmployerConsentRepository consentRepo;
    private final UserRepository userRepo;
    private final EmployerProfileRepository employerProfileRepo;

    public ConsentServiceImpl(EmployerConsentRepository consentRepo,
                              UserRepository userRepo,
                              EmployerProfileRepository employerProfileRepo) {
        this.consentRepo = consentRepo;
        this.userRepo = userRepo;
        this.employerProfileRepo = employerProfileRepo;
    }

    // -------------------------------------------------
    // EMPLOYER → REQUEST CONSENT
    // -------------------------------------------------
    @Override
    public void requestConsent(UUID employerId, UUID ownerId) {

        if (employerId.equals(ownerId)) {
            throw new RuntimeException("Cannot request consent from self");
        }

        // Validate users exist
        validateUser(employerId, User.Role.EMPLOYER);
        validateUser(ownerId, User.Role.INDIVIDUAL);

        consentRepo.findByEmployerIdAndOwnerId(employerId, ownerId)
            .ifPresent(existing -> {
                switch (existing.getStatus()) {
                    case REQUESTED ->
                        throw new RuntimeException("Consent already requested");
                    case APPROVED ->
                        throw new RuntimeException("Consent already approved");
                    case REVOKED, EXPIRED -> {
                        // revive request
                        existing.setStatus(EmployerConsent.Status.REQUESTED);
                        existing.setCreatedAt(Instant.now());
                        consentRepo.save(existing);
                        throw new SilentReturnException();
                    }
                }
            });

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(ownerId);
        consent.setStatus(EmployerConsent.Status.REQUESTED);
        consent.setCreatedAt(Instant.now());

        consentRepo.save(consent);
    }

    // -------------------------------------------------
    // INDIVIDUAL → APPROVE CONSENT
    // -------------------------------------------------
    @Override
    public void approveConsent(UUID ownerId, UUID employerId) {

        EmployerConsent consent = consentRepo
            .findByEmployerIdAndOwnerId(employerId, ownerId)
            .orElseThrow(() -> new RuntimeException("Consent not found"));

        if (consent.getStatus() != EmployerConsent.Status.REQUESTED) {
            throw new RuntimeException("Consent not in REQUESTED state");
        }

        consent.setStatus(EmployerConsent.Status.APPROVED);
        consent.setValidFrom(Instant.now());
        consent.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS));

        consentRepo.save(consent);
    }

    // -------------------------------------------------
    // INDIVIDUAL → REVOKE CONSENT
    // -------------------------------------------------
    @Override
    public void revokeConsent(UUID ownerId, UUID employerId) {

        EmployerConsent consent = consentRepo
            .findByEmployerIdAndOwnerId(employerId, ownerId)
            .orElseThrow(() -> new RuntimeException("Consent not found"));

        if (consent.getStatus() != EmployerConsent.Status.APPROVED) {
            throw new RuntimeException("Only approved consent can be revoked");
        }

        consent.setStatus(EmployerConsent.Status.REVOKED);
        consentRepo.save(consent);
    }

    // -------------------------------------------------
    // DOCUMENT ACCESS CHECK
    // -------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveConsent(UUID employerId, UUID ownerId) {

        return consentRepo.findByEmployerIdAndOwnerId(employerId, ownerId)
            .filter(c ->
                c.getStatus() == EmployerConsent.Status.APPROVED &&
                (c.getValidUntil() == null ||
                 Instant.now().isBefore(c.getValidUntil()))
            )
            .isPresent();
    }

    // -------------------------------------------------
    // INTERNAL VALIDATION
    // -------------------------------------------------
    private void validateUser(UUID userId, User.Role expectedRole) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != expectedRole) {
            throw new RuntimeException("Invalid role for consent operation");
        }
    }

    /**
     * Used internally to exit early after revive logic
     */
    private static class SilentReturnException extends RuntimeException {
        private SilentReturnException() {}
    }

    @Override
    public ConsentViewModel getConsents(UUID ownerId) {
        List<ConsentDTO> pending = consentRepo.findByOwnerIdAndStatus(ownerId, EmployerConsent.Status.REQUESTED).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());

        List<ConsentDTO> active = consentRepo.findByOwnerIdAndStatus(ownerId, EmployerConsent.Status.APPROVED).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());

        List<EmployerConsent.Status> historyStatuses = Arrays.asList(
            EmployerConsent.Status.REJECTED,
            EmployerConsent.Status.REVOKED,
            EmployerConsent.Status.EXPIRED
        );
        List<ConsentDTO> history = consentRepo.findByOwnerIdAndStatusIn(ownerId, historyStatuses).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());

        return new ConsentViewModel(pending, active, history);
    }

    private ConsentDTO mapToDTO(EmployerConsent consent) {
        String employerName = employerProfileRepo.findById(consent.getEmployerId())
            .map(EmployerProfile::getCompanyName)
            .orElse("Unknown Employer");

        // decisionAt: validFrom for APPROVED, or createdAt fallback for REJECTED/REVOKED
        Instant decisionAt = null;
        if (consent.getStatus() == EmployerConsent.Status.APPROVED && consent.getValidFrom() != null) {
            decisionAt = consent.getValidFrom();
        } else if (consent.getStatus() == EmployerConsent.Status.REJECTED ||
                   consent.getStatus() == EmployerConsent.Status.REVOKED) {
            decisionAt = consent.getCreatedAt();
        }

        ConsentDTO dto = new ConsentDTO();
        dto.setId(consent.getId());
        dto.setEmployerId(consent.getEmployerId());
        dto.setEmployerName(employerName);
        dto.setStatus(consent.getStatus());
        dto.setCreatedAt(consent.getCreatedAt());
        dto.setValidUntil(consent.getValidUntil());
        dto.setDecisionAt(decisionAt);
        return dto;
    }

    @Override
    public void approveConsentById(UUID consentId, UUID ownerId) {
        EmployerConsent consent = consentRepo.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consent not found"));

        if (!consent.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        consent.setStatus(EmployerConsent.Status.APPROVED);
        consent.setValidFrom(Instant.now());
        consent.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS));
        consentRepo.save(consent);
    }

    @Override
    public void rejectConsentById(UUID consentId, UUID ownerId) {
        EmployerConsent consent = consentRepo.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consent not found"));

        if (!consent.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        consent.setStatus(EmployerConsent.Status.REJECTED);
        consentRepo.save(consent);
    }

    @Override
    public void revokeConsentById(UUID consentId, UUID ownerId) {
        EmployerConsent consent = consentRepo.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consent not found"));

        if (!consent.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        consent.setStatus(EmployerConsent.Status.REVOKED);
        consentRepo.save(consent);
    }
}
