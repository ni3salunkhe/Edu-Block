package com.edu.servicesImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.edu.dto.CreateInstitutionRequest;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.entity.InstitutionVerificationAudit;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.InstitutionVerificationAuditRepository;
import com.edu.services.AuditService;
import com.edu.services.InstitutionService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepo;
    private final InstitutionVerificationAuditRepository verificationAuditRepo;
    private final AuditService auditService;

    public InstitutionServiceImpl(InstitutionRepository institutionRepo,
                                  InstitutionVerificationAuditRepository verificationAuditRepo,
                                  AuditService auditService) {
        this.institutionRepo = institutionRepo;
        this.verificationAuditRepo = verificationAuditRepo;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public Institution createInstitution(CreateInstitutionRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Institution code cannot be null or empty");
        }
        if (request.getOfficialEmail() == null || request.getOfficialEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Institution official email cannot be null or empty");
        }

        // Normalize code: trim and uppercase
        String normalizedCode = request.getCode().trim().toUpperCase();
        String email = request.getOfficialEmail().trim();

        // Validate uniqueness
        if (institutionRepo.existsByCode(normalizedCode)) {
            throw new IllegalArgumentException("Institution code already exists");
        }
        if (institutionRepo.existsByOfficialEmail(email)) {
            throw new IllegalArgumentException("Institution email already exists");
        }

        Institution institution = new Institution();
        institution.setName(request.getName());
        institution.setCode(normalizedCode);
        institution.setWebsite(request.getWebsite());
        institution.setOfficialEmail(email);
        institution.setCity(request.getCity());
        institution.setState(request.getState());
        institution.setCountry(request.getCountry());

        // Assign defaults inside service layer
        institution.setTrustLevel(InstitutionTrustLevel.REGISTERED);
        institution.setStatus(InstitutionStatus.ACTIVE);

        Institution saved = institutionRepo.save(institution);

        // Audit Logging
        UUID adminId = null;
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
                adminId = UUID.fromString(
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
                );
            }
        } catch (Exception e) {
            // Log warning or keep adminId as null if no authentication is found (e.g. in test setup)
        }

        String requestIp = "0.0.0.0";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest servletRequest = attributes.getRequest();
                if (servletRequest != null) {
                    requestIp = servletRequest.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            // fallback
        }

        auditService.logAdminAction(
            adminId != null ? adminId : UUID.randomUUID(), // fallback if null for logs
            "CREATE_INSTITUTION",
            "INSTITUTION",
            saved.getId(),
            "{\"name\":\"" + saved.getName() + "\",\"code\":\"" + saved.getCode() + "\"}",
            requestIp
        );

        return saved;
    }

    @Override
    public Institution getInstitution(UUID id) {
        return institutionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
    }

    @Override
    public List<Institution> listInstitutions() {
        return institutionRepo.findAll();
    }

    @Override
    @Transactional
    public Institution verifyInstitution(UUID institutionId, String reason, UUID adminId) {
        validateReason(reason);
        validateAdmin(adminId);

        Institution institution = getInstitution(institutionId);
        validateActiveStatus(institution);

        InstitutionTrustLevel oldLevel = institution.getTrustLevel();
        if (oldLevel != InstitutionTrustLevel.REGISTERED) {
            throw new IllegalArgumentException("Invalid transition: current trust level must be REGISTERED to verify");
        }

        InstitutionTrustLevel newLevel = InstitutionTrustLevel.VERIFIED;
        institution.setTrustLevel(newLevel);
        Institution saved = institutionRepo.save(institution);

        saveVerificationAudit(saved, oldLevel, newLevel, adminId, reason);
        logSystemAudit("VERIFY_INSTITUTION", saved, oldLevel, newLevel, adminId, reason);

        return saved;
    }

    @Override
    @Transactional
    public Institution accreditInstitution(UUID institutionId, String reason, UUID adminId) {
        validateReason(reason);
        validateAdmin(adminId);

        Institution institution = getInstitution(institutionId);
        validateActiveStatus(institution);

        InstitutionTrustLevel oldLevel = institution.getTrustLevel();
        if (oldLevel != InstitutionTrustLevel.VERIFIED) {
            throw new IllegalArgumentException("Invalid transition: current trust level must be VERIFIED to accredit");
        }

        InstitutionTrustLevel newLevel = InstitutionTrustLevel.ACCREDITED;
        institution.setTrustLevel(newLevel);
        Institution saved = institutionRepo.save(institution);

        saveVerificationAudit(saved, oldLevel, newLevel, adminId, reason);
        logSystemAudit("ACCREDIT_INSTITUTION", saved, oldLevel, newLevel, adminId, reason);

        return saved;
    }

    @Override
    public List<InstitutionVerificationAudit> getAuditHistory(UUID institutionId) {
        Institution institution = getInstitution(institutionId);
        return verificationAuditRepo.findByInstitutionOrderByChangedAtDesc(institution);
    }

    private void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
        if (reason.trim().length() < 10) {
            throw new IllegalArgumentException("Reason must be at least 10 characters");
        }
    }

    private void validateAdmin(UUID adminId) {
        if (adminId == null) {
            throw new IllegalArgumentException("Admin identity must be authenticated");
        }
    }

    private void validateActiveStatus(Institution institution) {
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {
            throw new IllegalArgumentException("Institution must be ACTIVE for trust level transition");
        }
    }

    private void saveVerificationAudit(Institution institution, InstitutionTrustLevel oldLevel,
                                        InstitutionTrustLevel newLevel, UUID adminId, String reason) {
        InstitutionVerificationAudit audit = new InstitutionVerificationAudit();
        audit.setInstitution(institution);
        audit.setOldTrustLevel(oldLevel);
        audit.setNewTrustLevel(newLevel);
        audit.setChangedBy(adminId);
        audit.setReason(reason);
        verificationAuditRepo.save(audit);
    }

    private void logSystemAudit(String action, Institution institution, InstitutionTrustLevel oldLevel,
                                InstitutionTrustLevel newLevel, UUID adminId, String reason) {
        String requestIp = "0.0.0.0";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest servletRequest = attributes.getRequest();
                if (servletRequest != null) {
                    requestIp = servletRequest.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            // fallback
        }

        String metadata = String.format(
            "{\"oldTrustLevel\":\"%s\",\"newTrustLevel\":\"%s\",\"reason\":\"%s\"}",
            oldLevel.name(), newLevel.name(), reason.replace("\"", "\\\"")
        );

        auditService.logAdminAction(
            adminId,
            action,
            "INSTITUTION",
            institution.getId(),
            metadata,
            requestIp
        );
    }
}

