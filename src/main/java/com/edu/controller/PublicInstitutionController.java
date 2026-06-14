package com.edu.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.InstitutionResponseDTO;
import com.edu.entity.Institution;
import com.edu.services.InstitutionService;

@RestController
@RequestMapping("/api/institutions")
public class PublicInstitutionController {

    private final InstitutionService institutionService;

    public PublicInstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    public List<InstitutionResponseDTO> listInstitutions() {
        return institutionService.listInstitutions().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private InstitutionResponseDTO mapToDTO(Institution inst) {
        return new InstitutionResponseDTO(
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
    }
}
