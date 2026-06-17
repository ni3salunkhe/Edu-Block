package com.edu.controller;

import com.edu.dto.DocumentIntelligenceDTO;
import com.edu.services.DocumentIntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{id}/intelligence")
public class DocumentIntelligenceController {

    private final DocumentIntelligenceService intelligenceService;

    public DocumentIntelligenceController(DocumentIntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'VERIFIER', 'EMPLOYER')")
    public ResponseEntity<DocumentIntelligenceDTO> getIntelligence(@PathVariable UUID id) {
        DocumentIntelligenceDTO dto = intelligenceService.getIntelligence(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}
