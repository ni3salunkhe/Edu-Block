package com.edu.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.RejectOrRevokeRequest;
import com.edu.dto.InstitutionAssociationResponseDTO;
import com.edu.entity.AssociationStatus;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionAssociation;
import com.edu.services.InstitutionAssociationService;
import com.edu.services.InstitutionUserService;

@RestController
@RequestMapping("/api/institution")
@PreAuthorize("hasRole('INSTITUTION')")
public class InstitutionAssociationController {

    private final InstitutionAssociationService associationService;
    private final InstitutionUserService institutionUserService;

    public InstitutionAssociationController(InstitutionAssociationService associationService,
                                            InstitutionUserService institutionUserService) {
        this.associationService = associationService;
        this.institutionUserService = institutionUserService;
    }

    @GetMapping("/associations/pending")
    public List<InstitutionAssociationResponseDTO> getPendingAssociations() {
        UUID institutionUserId = getAuthenticatedInstitutionUserId();
        Institution institution = institutionUserService.getInstitutionForUser(institutionUserId)
                .orElseThrow(() -> new AccessDeniedException("User is not mapped to any institution"));

        return associationService.getInstitutionAssociations(institution.getId()).stream()
                .filter(assoc -> assoc.getStatus() == AssociationStatus.PENDING)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/associations")
    public List<InstitutionAssociationResponseDTO> getAssociations() {
        UUID institutionUserId = getAuthenticatedInstitutionUserId();
        Institution institution = institutionUserService.getInstitutionForUser(institutionUserId)
                .orElseThrow(() -> new AccessDeniedException("User is not mapped to any institution"));

        return associationService.getInstitutionAssociations(institution.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/associations/{id}/approve")
    public InstitutionAssociationResponseDTO approveAssociation(@PathVariable UUID id) {
        UUID institutionUserId = getAuthenticatedInstitutionUserId();
        InstitutionAssociation approved = associationService.approveAssociation(id, institutionUserId);
        return mapToDTO(approved);
    }

    @PostMapping("/associations/{id}/reject")
    public InstitutionAssociationResponseDTO rejectAssociation(@PathVariable UUID id,
                                                               @RequestBody RejectOrRevokeRequest request) {
        UUID institutionUserId = getAuthenticatedInstitutionUserId();
        InstitutionAssociation rejected = associationService.rejectAssociation(id, institutionUserId, request.getReason());
        return mapToDTO(rejected);
    }

    @PostMapping("/associations/{id}/revoke")
    public InstitutionAssociationResponseDTO revokeAssociation(@PathVariable UUID id,
                                                               @RequestBody RejectOrRevokeRequest request) {
        UUID institutionUserId = getAuthenticatedInstitutionUserId();
        InstitutionAssociation revoked = associationService.revokeAssociation(id, institutionUserId, request.getReason());
        return mapToDTO(revoked);
    }

    private UUID getAuthenticatedInstitutionUserId() {
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

    private InstitutionAssociationResponseDTO mapToDTO(InstitutionAssociation association) {
        return new InstitutionAssociationResponseDTO(
            association.getId(),
            association.getStudent().getId(),
            association.getStudent().getUsername(),
            association.getStudent().getEmail(),
            association.getInstitution().getId(),
            association.getInstitution().getName(),
            association.getEnrollmentNumber(),
            association.getStatus(),
            association.getReviewedBy(),
            association.getReviewReason(),
            association.getRequestedAt(),
            association.getReviewedAt()
        );
    }
}
