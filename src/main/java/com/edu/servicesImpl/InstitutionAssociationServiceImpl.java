package com.edu.servicesImpl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.edu.entity.AssociationStatus;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionAssociation;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;
import com.edu.repositories.InstitutionAssociationRepository;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.InstitutionUserRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.AuditService;
import com.edu.services.InstitutionAssociationService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class InstitutionAssociationServiceImpl implements InstitutionAssociationService {

    private final UserRepository userRepo;
    private final InstitutionRepository institutionRepo;
    private final InstitutionAssociationRepository associationRepo;
    private final InstitutionUserRepository institutionUserRepo;
    private final AuditService auditService;

    public InstitutionAssociationServiceImpl(UserRepository userRepo,
                                             InstitutionRepository institutionRepo,
                                             InstitutionAssociationRepository associationRepo,
                                             InstitutionUserRepository institutionUserRepo,
                                             AuditService auditService) {
        this.userRepo = userRepo;
        this.institutionRepo = institutionRepo;
        this.associationRepo = associationRepo;
        this.institutionUserRepo = institutionUserRepo;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public InstitutionAssociation requestAssociation(UUID studentId, UUID institutionId, String enrollmentNumber) {
        if (enrollmentNumber == null || enrollmentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment number is required and cannot be empty");
        }
        String trimmedEnrollment = enrollmentNumber.trim();

        // 1. Validate student and role INDIVIDUAL
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (student.getRole() != User.Role.INDIVIDUAL) {
            throw new IllegalArgumentException("Only users with role INDIVIDUAL may request association");
        }

        // 2. Validate institution status and trust level
        Institution institution = institutionRepo.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {
            throw new IllegalArgumentException("Institution must be ACTIVE to request association");
        }
        if (institution.getTrustLevel() != InstitutionTrustLevel.ACCREDITED) {
            throw new IllegalArgumentException("Institution must be ACCREDITED to request association");
        }

        // 3. Duplicate pending request check
        if (associationRepo.existsByStudentAndInstitutionAndStatus(student, institution, AssociationStatus.PENDING)) {
            throw new IllegalArgumentException("Duplicate pending request exists for this institution");
        }

        // 4. Student active association check (only one APPROVED association)
        if (associationRepo.existsByStudentAndStatus(student, AssociationStatus.APPROVED)) {
            throw new IllegalArgumentException("Student already has an active approved association");
        }

        InstitutionAssociation association = new InstitutionAssociation();
        association.setStudent(student);
        association.setInstitution(institution);
        association.setEnrollmentNumber(trimmedEnrollment);
        association.setStatus(AssociationStatus.PENDING);

        InstitutionAssociation saved = associationRepo.save(association);

        // Audit Logging
        String requestIp = getClientIp();
        String metadata = String.format("{\"enrollmentNumber\":\"%s\"}", trimmedEnrollment);
        auditService.logIndividualAction(
            studentId,
            "ASSOCIATION_REQUESTED",
            "ASSOCIATION",
            saved.getId(),
            metadata,
            requestIp
        );

        return saved;
    }

    @Override
    @Transactional
    public InstitutionAssociation approveAssociation(UUID associationId, UUID institutionUserId) {
        InstitutionAssociation association = associationRepo.findById(associationId)
                .orElseThrow(() -> new RuntimeException("Association not found"));

        // 1. Validate status PENDING
        if (association.getStatus() != AssociationStatus.PENDING) {
            throw new IllegalArgumentException("Association is not in PENDING state");
        }

        // 2 & 3. Validate mapping and institution match
        InstitutionUser mapping = validateAndGetInstitutionUserMapping(institutionUserId, association.getInstitution().getId());

        // 4 & 5. Validate Institution status and trust level
        Institution institution = association.getInstitution();
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {
            throw new IllegalArgumentException("Institution must be ACTIVE");
        }
        if (institution.getTrustLevel() != InstitutionTrustLevel.ACCREDITED) {
            throw new IllegalArgumentException("Institution must be ACCREDITED");
        }

        // 6. Concurrency Protection / Active check
        User student = association.getStudent();
        if (associationRepo.existsByStudentAndStatus(student, AssociationStatus.APPROVED)) {
            throw new IllegalArgumentException("Student already has another APPROVED association");
        }

        association.setStatus(AssociationStatus.APPROVED);
        association.setReviewedBy(institutionUserId);
        association.setReviewedAt(Instant.now());

        InstitutionAssociation saved = associationRepo.save(association);

        // Audit Log
        logReviewAudit("ASSOCIATION_APPROVED", saved, institutionUserId, null);

        return saved;
    }

    @Override
    @Transactional
    public InstitutionAssociation rejectAssociation(UUID associationId, UUID institutionUserId, String reason) {
        validateReason(reason);

        InstitutionAssociation association = associationRepo.findById(associationId)
                .orElseThrow(() -> new RuntimeException("Association not found"));

        // 1. Validate status PENDING
        if (association.getStatus() != AssociationStatus.PENDING) {
            throw new IllegalArgumentException("Association is not in PENDING state");
        }

        // 2. Validate mapping and institution match
        validateAndGetInstitutionUserMapping(institutionUserId, association.getInstitution().getId());

        association.setStatus(AssociationStatus.REJECTED);
        association.setReviewReason(reason);
        association.setReviewedBy(institutionUserId);
        association.setReviewedAt(Instant.now());

        InstitutionAssociation saved = associationRepo.save(association);

        // Audit Log
        logReviewAudit("ASSOCIATION_REJECTED", saved, institutionUserId, reason);

        return saved;
    }

    @Override
    @Transactional
    public InstitutionAssociation revokeAssociation(UUID associationId, UUID institutionUserId, String reason) {
        validateReason(reason);

        InstitutionAssociation association = associationRepo.findById(associationId)
                .orElseThrow(() -> new RuntimeException("Association not found"));

        // 1. Validate status APPROVED
        if (association.getStatus() != AssociationStatus.APPROVED) {
            throw new IllegalArgumentException("Association is not in APPROVED state");
        }

        // 2. Validate mapping and institution match
        validateAndGetInstitutionUserMapping(institutionUserId, association.getInstitution().getId());

        // 4. Validate institution still ACTIVE
        Institution institution = association.getInstitution();
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {
            throw new IllegalArgumentException("Institution must be ACTIVE");
        }

        association.setStatus(AssociationStatus.REVOKED);
        association.setReviewReason(reason);
        association.setReviewedBy(institutionUserId);
        association.setReviewedAt(Instant.now());

        InstitutionAssociation saved = associationRepo.save(association);

        // Audit Log
        logReviewAudit("ASSOCIATION_REVOKED", saved, institutionUserId, reason);

        return saved;
    }

    @Override
    public List<InstitutionAssociation> getStudentAssociations(UUID studentId) {
        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return associationRepo.findByStudent(student);
    }

    @Override
    public List<InstitutionAssociation> getInstitutionAssociations(UUID institutionId) {
        Institution institution = institutionRepo.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        return associationRepo.findByInstitution(institution);
    }

    private InstitutionUser validateAndGetInstitutionUserMapping(UUID userId, UUID targetInstitutionId) {
        InstitutionUser mapping = institutionUserRepo.findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("User is not mapped to any institution"));

        if (!mapping.getInstitution().getId().equals(targetInstitutionId)) {
            throw new AccessDeniedException("You are not authorized to manage associations for this institution");
        }

        return mapping;
    }

    private void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required");
        }
        String trimmed = reason.trim();
        if (trimmed.length() < 10 || trimmed.length() > 1000) {
            throw new IllegalArgumentException("Reason must be between 10 and 1000 characters");
        }
    }

    private String getClientIp() {
        String ip = "0.0.0.0";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    ip = request.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return ip;
    }

    private void logReviewAudit(String action, InstitutionAssociation association, UUID reviewerId, String reason) {
        String ip = getClientIp();
        String metadata = reason != null 
            ? String.format("{\"reason\":\"%s\"}", reason.replace("\"", "\\\""))
            : "{}";

        auditService.logAdminAction(
            reviewerId,
            action,
            "ASSOCIATION",
            association.getId(),
            metadata,
            ip
        );
    }
}
