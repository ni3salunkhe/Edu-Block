package com.edu.services;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.edu.entity.Document;

public interface DocumentService {
	public void upload(MultipartFile file, String type);
	List<Document> getPendingForVerification();
	void approve(UUID documentId);
	void reject(UUID documentId);

}
