package com.edu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.services.DocumentService;

@RestController
@RequestMapping("/api/verifier")
public class VerifierController {

	
    @Autowired
    private DocumentService documentService;

    // 1️⃣ View pending documents
    @GetMapping("/pending")
    public List<com.edu.entity.Document> pending() {
        return documentService.getPendingForVerification();
    }

    // 2️⃣ Approve document
    @PostMapping("/approve/{id}")
    public void approve(@PathVariable UUID id) {
        documentService.approve(id);
    }

    // 3️⃣ Reject document
    @PostMapping("/reject/{id}")
    public void reject(@PathVariable UUID id) {
        documentService.reject(id);
    }
}
