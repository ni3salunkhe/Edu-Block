package com.edu.services;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import com.edu.entity.Document;


public interface DocumentService {
	public Document upload(MultipartFile file, String type);
	List<Document> getPendingForVerification();
	void approve(UUID documentId);
	void rejectWithComment(UUID documentId, String comment);
	void acknowledgeRejection(UUID documentId);
	void assertCanAccess(Document document);
	Document getDocumentById(UUID documentId);
	List<com.edu.dto.DocumentDTO> getMyDocuments();
	
	List<com.edu.dto.DocumentDTO> getDocumentsByConsent(UUID consentId, UUID employerId);
	com.edu.dto.DocumentDTO getDocumentDTO(UUID documentId);
}
