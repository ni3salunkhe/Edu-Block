package com.edu.controller;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.dto.VerifierDocumentDTO;
import com.edu.dto.VerifierSummaryDTO;
import com.edu.entity.Document;
import com.edu.entity.User;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.DocumentService;

@RestController
@RequestMapping("/api/verifier")
@PreAuthorize("hasRole('VERIFIER')")
public class VerifierController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/summary")
    public VerifierSummaryDTO getSummary() {
        long pending = documentRepository.countByStatusIn(Arrays.asList(Document.Status.AI_VERIFIED, Document.Status.AI_FLAGGED));
        long approved = documentRepository.countByStatus(Document.Status.APPROVED);
        long rejected = documentRepository.countByStatus(Document.Status.REJECTED);

        return new VerifierSummaryDTO(pending, approved, rejected);
    }

    @GetMapping("/queue")
    public List<VerifierDocumentDTO> getQueue() {
        return documentService.getPendingForVerification().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/document/{id}")
    public VerifierDocumentDTO getDocument(@PathVariable UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return mapToDTO(doc);
    }

    @PatchMapping("/document/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID id) {
        documentService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/document/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID id) {
        documentService.reject(id);
        return ResponseEntity.ok().build();
    }

    private VerifierDocumentDTO mapToDTO(Document doc) {
        String ownerName = userRepository.findById(doc.getOwnerId())
                .map(User::getUsername) // Assuming getUsername or fallback
                .orElse("Unknown Owner");

        String fileName = "document.pdf";
        try {
            fileName = Paths.get(doc.getFilePath()).getFileName().toString();
        } catch (Exception e) {}

        String aiStatus = (doc.getPerceptualHash() != null) ? 
            (doc.getMatchedDocumentId() != null ? "Flagged (Duplicate)" : "Verified (Unique)") 
            : "Processing";

        return new VerifierDocumentDTO(
                doc.getId(),
                fileName,
                ownerName,
                doc.getStatus().toString(),
                aiStatus,
                doc.getSha256Hash(),
                doc.getBlockchainTx(), // blockchainHash
                doc.getFilePath() // previewUrl
        );
    }
}
