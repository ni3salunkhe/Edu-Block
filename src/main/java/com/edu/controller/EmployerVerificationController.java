package com.edu.controller;

import com.edu.dto.EmployerDocumentViewDTO;
import com.edu.dto.DocumentTrustResponseDTO;
import com.edu.services.EmployerDocumentService;
import com.edu.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employer/verification")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerVerificationController {

    @Autowired
    private EmployerDocumentService employerDocumentService;

    @GetMapping("/documents")
    public List<EmployerDocumentViewDTO> getSharedDocuments(HttpServletRequest request) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        return employerDocumentService.getSharedDocuments(employerId, request.getRemoteAddr());
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<EmployerDocumentViewDTO> getDocumentDetails(@PathVariable UUID id, HttpServletRequest request) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        EmployerDocumentViewDTO dto = employerDocumentService.getDocumentDetails(employerId, id, request.getRemoteAddr());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/documents/{id}/trust")
    public ResponseEntity<DocumentTrustResponseDTO> getDocumentTrust(@PathVariable UUID id, HttpServletRequest request) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        DocumentTrustResponseDTO dto = employerDocumentService.getDocumentTrustDetails(employerId, id, request.getRemoteAddr());
        return ResponseEntity.ok(dto);
    }
}
