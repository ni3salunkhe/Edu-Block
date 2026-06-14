package com.edu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edu.dto.InstitutionIssuedDocumentDTO;
import com.edu.services.InstitutionCredentialService;

@RestController
@RequestMapping("/api/institution/credentials")
@PreAuthorize("hasRole('INSTITUTION')")
public class InstitutionCredentialController {

    private final InstitutionCredentialService credentialService;

    public InstitutionCredentialController(InstitutionCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping(value = "/issue", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionIssuedDocumentDTO issueDocument(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("studentId") UUID studentId,
                                                     @RequestParam("associationId") UUID associationId,
                                                     @RequestParam("documentType") String documentType,
                                                     @RequestParam("credentialReference") String credentialReference) {
        UUID institutionUserId = getAuthenticatedInstitutionUserId();
        return credentialService.issueDocument(
            institutionUserId, studentId, associationId, documentType, credentialReference, file
        );
    }

    @GetMapping
    public List<InstitutionIssuedDocumentDTO> getIssuedDocuments() {
        UUID institutionUserId = getAuthenticatedInstitutionUserId();
        return credentialService.getIssuedDocuments(institutionUserId);
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
}
