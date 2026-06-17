package com.edu.controller;

import com.edu.dto.DocumentVerificationResultDTO;
import com.edu.dto.FraudSignalDTO;
import com.edu.entity.DocumentVerificationResult;
import com.edu.repositories.DocumentVerificationResultRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('VERIFIER', 'ADMIN', 'INSTITUTION')")
public class DocumentVerificationController {

    private final DocumentVerificationResultRepository verificationResultRepository;
    private final ObjectMapper objectMapper;

    public DocumentVerificationController(DocumentVerificationResultRepository verificationResultRepository,
                                          ObjectMapper objectMapper) {
        this.verificationResultRepository = verificationResultRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{id}/verification")
    public ResponseEntity<DocumentVerificationResultDTO> getVerificationResult(@PathVariable UUID id) {
        return verificationResultRepository.findById(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private DocumentVerificationResultDTO mapToDTO(DocumentVerificationResult result) {
        DocumentVerificationResultDTO dto = new DocumentVerificationResultDTO();
        dto.setDocumentId(result.getDocumentId());
        dto.setConfidenceScore(result.getConfidenceScore());
        dto.setConfidenceLevel(result.getConfidenceLevel());
        dto.setRiskLevel(result.getRiskLevel());
        dto.setIsVerified(result.getIsVerified());
        dto.setVerificationModelVersion(result.getVerificationModelVersion());
        dto.setOcrTextHash(result.getOcrTextHash());
        dto.setProcessedAt(result.getProcessedAt());

        // Deserialize explanations JSON
        List<DocumentVerificationResultDTO.MapExplanation> explanationsList = new ArrayList<>();
        try {
            explanationsList = objectMapper.readValue(
                    result.getExplanationsJson(),
                    new TypeReference<List<DocumentVerificationResultDTO.MapExplanation>>() {}
            );
        } catch (Exception ignored) {}
        dto.setExplanations(explanationsList);

        // Map fraud signals
        List<FraudSignalDTO> signalsList = Collections.emptyList();
        if (result.getFraudSignals() != null) {
            signalsList = result.getFraudSignals().stream()
                    .map(fs -> new FraudSignalDTO(
                            fs.getId(),
                            fs.getSignalType(),
                            fs.getSeverity(),
                            fs.getDescription(),
                            fs.getEvidenceJson(),
                            fs.getDetectedAt()
                    ))
                    .collect(Collectors.toList());
        }
        dto.setFraudSignals(signalsList);

        return dto;
    }
}
