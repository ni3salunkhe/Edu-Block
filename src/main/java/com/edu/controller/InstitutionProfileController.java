package com.edu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.InstitutionResponseDTO;
import com.edu.entity.Institution;
import com.edu.services.InstitutionUserService;
import com.edu.util.SecurityUtil;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/institution")
@PreAuthorize("hasRole('INSTITUTION')")
public class InstitutionProfileController {

    private final InstitutionUserService institutionUserService;

    public InstitutionProfileController(InstitutionUserService institutionUserService) {
        this.institutionUserService = institutionUserService;
    }

    @GetMapping("/profile")
    public ResponseEntity<InstitutionResponseDTO> getProfile() {
        UUID userId = SecurityUtil.getCurrentUserId();
        Optional<Institution> institutionOpt = institutionUserService.getInstitutionForUser(userId);

        if (institutionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Institution inst = institutionOpt.get();
        InstitutionResponseDTO dto = new InstitutionResponseDTO(
                inst.getId(),
                inst.getName(),
                inst.getCode(),
                inst.getWebsite(),
                inst.getOfficialEmail(),
                inst.getCity(),
                inst.getState(),
                inst.getCountry(),
                inst.getTrustLevel(),
                inst.getStatus(),
                inst.getCreatedAt(),
                inst.getUpdatedAt()
        );

        return ResponseEntity.ok(dto);
    }
}
