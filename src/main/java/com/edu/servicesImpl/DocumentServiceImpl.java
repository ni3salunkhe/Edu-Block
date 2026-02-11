package com.edu.servicesImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tomcat.util.file.ConfigurationSource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import com.edu.config.StorageConfig;
import com.edu.dto.AIResult;
import com.edu.dto.SimilarityMatch;
import com.edu.entity.Document;
import com.edu.repositories.DocumentRepository;
import com.edu.services.AIService;
import com.edu.services.AuditService;
import com.edu.services.BlockchainService;
import com.edu.services.ConsentService;
import com.edu.services.DocumentAccessService;
import com.edu.services.DocumentService;
import com.edu.services.SimilarityMatchService;
import com.edu.util.SecurityUtil;

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

    @Autowired
    AuditService auditService;
    
    @Autowired
    ConsentService consentService;
    
    @Autowired
    DocumentAccessService documentAccessService;
    
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
            
	        UUID userId = SecurityUtil.getCurrentUserId();
	        doc.setOwnerId(userId);

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

        try {
            UUID verifierId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String requestIp = request.getRemoteAddr();
            
            auditService.logVerifierAction(
                verifierId,
                "APPROVE_DOCUMENT",
                "DOCUMENT",
                id,
                "{\"sha256\":\"" + doc.getSha256Hash() + "\"}",
                requestIp
            );
        } catch (Exception e) {
            // Log audit failure but don't fail operation
            e.printStackTrace();
        }
	}

	@Override
	public void reject(UUID id) {
	    Document doc = documentRepo.findById(id).orElseThrow();
	    doc.setStatus(Document.Status.REJECTED);
	    documentRepo.save(doc);

        try {
            UUID verifierId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String requestIp = request.getRemoteAddr();

            auditService.logVerifierAction(
                verifierId,
                "REJECT_DOCUMENT",
                "DOCUMENT",
                id,
                "{\"sha256\":\"" + doc.getSha256Hash() + "\"}",
                requestIp
            );
        } catch (Exception e) {
             e.printStackTrace();
        }
	}





	    @Override
	    public void assertCanAccess(Document doc) {
	        documentAccessService.assertCanAccess(doc);
	    }
	    
	    @Override
	    public Document getDocumentById(UUID documentId) {

	        Document doc = documentRepo.findById(documentId)
	            .orElseThrow(() -> new RuntimeException("Document not found"));

	        documentAccessService.assertCanAccess(doc);

	        return doc;
	    }
	    
	    @Override
	    public FileSystemResource downloadDocument(UUID documentId) {

	        Document doc = documentRepo.findById(documentId)
	            .orElseThrow(() -> new RuntimeException("Document not found"));

	        documentAccessService.assertCanAccess(doc);

	        Path filePath = java.nio.file.Paths.get(doc.getFilePath());
	        return new FileSystemResource(filePath);
	    }
	    
	    @Override
	    public List<Document> getMyDocuments() {

	        UUID ownerId = SecurityUtil.getCurrentUserId();
	        String role = SecurityUtil.getCurrentUserRole();

	        if (!"INDIVIDUAL".equals(role)) {
	            throw new AccessDeniedException("Only individuals can list own documents");
	        }

	        return documentRepo.findByOwnerId(ownerId);
	    }


}
