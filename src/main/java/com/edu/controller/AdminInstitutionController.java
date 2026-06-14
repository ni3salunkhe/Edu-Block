package com.edu.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.CreateInstitutionRequest;
import com.edu.dto.InstitutionResponseDTO;
import com.edu.dto.InstitutionTrustUpdateRequest;
import com.edu.dto.InstitutionAuditResponseDTO;
import com.edu.entity.Institution;
import com.edu.services.InstitutionService;

@RestController
@RequestMapping("/api/admin/institutions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInstitutionController {

    private final InstitutionService institutionService;

    public AdminInstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionResponseDTO createInstitution(@RequestBody CreateInstitutionRequest request) {
        Institution created = institutionService.createInstitution(request);
        return mapToDTO(created);
    }

    @GetMapping
    public List<InstitutionResponseDTO> listInstitutions() {
        return institutionService.listInstitutions().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public InstitutionResponseDTO getInstitution(@PathVariable UUID id) {
        Institution institution = institutionService.getInstitution(id);
        return mapToDTO(institution);
    }

    @PostMapping("/{id}/verify")
    public InstitutionResponseDTO verifyInstitution(@PathVariable UUID id,
                                                    @RequestBody InstitutionTrustUpdateRequest request) {
        UUID adminId = getAuthenticatedAdminId();
        Institution updated = institutionService.verifyInstitution(id, request.getReason(), adminId);
        return mapToDTO(updated);
    }

    @PostMapping("/{id}/accredit")
    public InstitutionResponseDTO accreditInstitution(@PathVariable UUID id,
                                                      @RequestBody InstitutionTrustUpdateRequest request) {
        UUID adminId = getAuthenticatedAdminId();
        Institution updated = institutionService.accreditInstitution(id, request.getReason(), adminId);
        return mapToDTO(updated);
    }

    @GetMapping("/{id}/audit-history")
    public List<InstitutionAuditResponseDTO> getAuditHistory(@PathVariable UUID id) {
        return institutionService.getAuditHistory(id).stream()
                .map(audit -> new InstitutionAuditResponseDTO(
                    audit.getOldTrustLevel(),
                    audit.getNewTrustLevel(),
                    audit.getReason(),
                    audit.getChangedBy(),
                    audit.getChangedAt()
                ))
                .collect(Collectors.toList());
    }

    private UUID getAuthenticatedAdminId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
            SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
            throw new IllegalArgumentException("Admin identity must be authenticated");
        }
        try {
            return UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid admin principal");
        }
    }

    private InstitutionResponseDTO mapToDTO(Institution institution) {
        return new InstitutionResponseDTO(
            institution.getId(),
            institution.getName(),
            institution.getCode(),
            institution.getWebsite(),
            institution.getOfficialEmail(),
            institution.getCity(),
            institution.getState(),
            institution.getCountry(),
            institution.getTrustLevel(),
            institution.getStatus(),
            institution.getCreatedAt(),
            institution.getUpdatedAt()
        );
    }
}

