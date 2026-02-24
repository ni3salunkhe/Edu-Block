package com.edu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edu.dto.DocumentDTO;
import com.edu.entity.Document;
import com.edu.services.DocumentService;
import com.edu.util.SecurityUtil;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	@Autowired
	DocumentService documentService;
	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam MultipartFile file,
	                                @RequestParam String documentType) {
	    documentService.upload(file, documentType);
	    return ResponseEntity.ok("Uploaded");
	}

	@GetMapping("/{id}")
	public ResponseEntity<Document> getDocument(@PathVariable UUID id) {
	    return ResponseEntity.ok(documentService.getDocumentById(id));
	}

    @GetMapping("/my")
    public List<DocumentDTO> getMyDocuments() {
        return documentService.getMyDocuments();
    }

    @GetMapping("/by-consent/{consentId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public List<DocumentDTO> getDocumentsByConsent(@PathVariable UUID consentId) {
        UUID employerId = SecurityUtil.getCurrentUserId();

        return documentService.getDocumentsByConsent(consentId, employerId);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Void> viewDocument(@PathVariable UUID id) {
        Document doc = documentService.getDocumentById(id);
        return ResponseEntity.status(302)
            .header("Location", doc.getFilePath())
            .build();
    }

    /**
     * Called by the individual user AFTER reading the rejection reason.
     * Permanently deletes the rejected document record from the database.
     */
    @DeleteMapping("/{id}/acknowledge-rejection")
    public ResponseEntity<Void> acknowledgeRejection(@PathVariable UUID id) {
        documentService.acknowledgeRejection(id);
        return ResponseEntity.noContent().build();
    }
}
