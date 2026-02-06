package com.edu.servicesImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.edu.config.StorageConfig;
import com.edu.dto.AIResult;
import com.edu.dto.SimilarityMatch;
import com.edu.entity.Document;
import com.edu.repositories.DocumentRepository;
import com.edu.services.AIService;
import com.edu.services.BlockchainService;
import com.edu.services.DocumentService;
import com.edu.services.SimilarityMatchService;

@Service
public class DocumentServiceImpl implements DocumentService {
	
	@Autowired
	StorageConfig storageConfig;
	
	@Autowired
	DocumentRepository documentRepo;
	
	@Autowired
	BlockchainService blockchainService;
	
	@Autowired
	AIService aiService;
	
	@Autowired
	SimilarityMatchService similarityService;
	
	@Override
	public void upload(MultipartFile file, String documentType) {

	    try {
	        byte[] bytes = file.getBytes();

	        // 1. Hash
	        String sha256 = DigestUtils.sha256Hex(bytes);

	        if (documentRepo.existsBySha256Hash(sha256)) {
	            throw new RuntimeException("Exact duplicate document");
	            
	        }
	        
	        // 2. Store on disk
	        Path path = ((Path) storageConfig.getRoot())
	                .resolve(UUID.randomUUID() + ".pdf");
	        Files.write(path, bytes);

	        // 3. AI analysis
	        AIResult result = aiService.analyze(path.toFile());

	        SimilarityMatch match = similarityService.findSimilar(
	                result.getPerceptualHash()
	        );

	        Document doc = new Document();
	        doc.setSha256Hash(sha256);
	        doc.setPerceptualHash(result.getPerceptualHash());
	        doc.setFilePath(path.toString());
	        doc.setCreatedAt(Instant.now());

	        if (match != null) {
	            doc.setStatus(Document.Status.AI_FLAGGED);
	            doc.setMatchedDocumentId(match.getDocumentId());
	        } else {
	            doc.setStatus(Document.Status.AI_VERIFIED);
	        }

	        documentRepo.save(doc);

	    } catch (IOException e) {
	        throw new RuntimeException("Document upload failed", e);
	    }
	}
	
	@Override
	public List<Document> getPendingForVerification() {
	    return documentRepo.findByStatusIn(
	        List.of(Document.Status.AI_VERIFIED, Document.Status.AI_FLAGGED)
	    );
	}

	@Override
	public void approve(UUID id) {
	    Document doc = documentRepo.findById(id).orElseThrow();

	    doc.setStatus(Document.Status.APPROVED);
	    documentRepo.save(doc);

	    // 🔥 blockchain anchoring happens here
	    blockchainService.anchor(doc);
	    System.out.print("Blockchain Anchoring");
	}

	@Override
	public void reject(UUID id) {
	    Document doc = documentRepo.findById(id).orElseThrow();
	    doc.setStatus(Document.Status.REJECTED);
	    documentRepo.save(doc);
	}


}
