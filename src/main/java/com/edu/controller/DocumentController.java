package com.edu.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edu.entity.Document;
import com.edu.services.DocumentService;

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

}
