package com.edu.servicesImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.edu.entity.Institution;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.InstitutionUserRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.AuditService;
import com.edu.services.InstitutionUserService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class InstitutionUserServiceImpl implements InstitutionUserService {

    private final UserRepository userRepo;
    private final InstitutionRepository institutionRepo;
    private final InstitutionUserRepository institutionUserRepo;
    private final AuditService auditService;

    public InstitutionUserServiceImpl(UserRepository userRepo,
                                      InstitutionRepository institutionRepo,
                                      InstitutionUserRepository institutionUserRepo,
                                      AuditService auditService) {
        this.userRepo = userRepo;
        this.institutionRepo = institutionRepo;
        this.institutionUserRepo = institutionUserRepo;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public InstitutionUser mapUserToInstitution(UUID userId, UUID institutionId) {
        // 1. Check if user exists
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check user role
        if (user.getRole() != User.Role.INSTITUTION) {
            throw new IllegalArgumentException("User role must be INSTITUTION");
        }

        // 3. Check if institution exists
        Institution institution = institutionRepo.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        // 4. Check institution status
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {
            throw new IllegalArgumentException("Institution must be ACTIVE");
        }

        // 5. Check duplicate mapping
        if (institutionUserRepo.existsByUserId(userId)) {
            throw new IllegalArgumentException("User is already mapped to an institution");
        }

        InstitutionUser mapping = new InstitutionUser();
        mapping.setUser(user);
        mapping.setInstitution(institution);

        InstitutionUser saved = institutionUserRepo.save(mapping);

        // Audit log
        logSystemAudit("MAP_INSTITUTION_USER", saved.getId(), "{\"userId\":\"" + userId + "\",\"institutionId\":\"" + institutionId + "\"}");

        return saved;
    }

    @Override
    public Optional<Institution> getInstitutionForUser(UUID userId) {
        return institutionUserRepo.findByUserId(userId)
                .map(InstitutionUser::getInstitution);
    }

    @Override
    public List<User> getUsersForInstitution(UUID institutionId) {
        Institution institution = institutionRepo.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        return institutionUserRepo.findByInstitution(institution).stream()
                .map(InstitutionUser::getUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeUserFromInstitution(UUID userId) {
        InstitutionUser mapping = institutionUserRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Mapping not found"));

        institutionUserRepo.delete(mapping);

        // Audit log
        logSystemAudit("UNMAP_INSTITUTION_USER", userId, "{\"userId\":\"" + userId + "\",\"institutionId\":\"" + mapping.getInstitution().getId() + "\"}");
    }

    private void logSystemAudit(String action, UUID targetId, String metadata) {
        UUID adminId = null;
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
                adminId = UUID.fromString(
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
                );
            }
        } catch (Exception e) {
            // fallback
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
            adminId != null ? adminId : UUID.randomUUID(),
            action,
            "INSTITUTION_USER",
            targetId,
            metadata,
            requestIp
        );
    }
}
