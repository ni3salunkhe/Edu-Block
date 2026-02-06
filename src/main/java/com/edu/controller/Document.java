package com.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edu.services.DocumentService;

@RestController
@RequestMapping("/api/documents")
public class Document {

	@Autowired
	DocumentService documentService;
	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam MultipartFile file,
	                                @RequestParam String documentType) {
	    documentService.upload(file, documentType);
	    return ResponseEntity.ok("Uploaded");
	}

}
