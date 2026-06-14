package com.edu.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.AssociateRequest;
import com.edu.dto.InstitutionAssociationResponseDTO;
import com.edu.entity.InstitutionAssociation;
import com.edu.services.InstitutionAssociationService;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('INDIVIDUAL')")
public class StudentAssociationController {

    private final InstitutionAssociationService associationService;

    public StudentAssociationController(InstitutionAssociationService associationService) {
        this.associationService = associationService;
    }

    @PostMapping("/institutions/{institutionId}/associate")
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionAssociationResponseDTO requestAssociation(@PathVariable UUID institutionId,
                                                                @RequestBody AssociateRequest request) {
        UUID studentId = getAuthenticatedStudentId();
        InstitutionAssociation association = associationService.requestAssociation(studentId, institutionId, request.getEnrollmentNumber());
        return mapToDTO(association);
    }

    @GetMapping("/associations")
    public List<InstitutionAssociationResponseDTO> getStudentAssociations() {
        UUID studentId = getAuthenticatedStudentId();
        return associationService.getStudentAssociations(studentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UUID getAuthenticatedStudentId() {
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
