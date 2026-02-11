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

import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.edu.dto.AIResult;
import com.edu.dto.DocumentDTO;
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
import com.edu.services.StorageService;
import com.edu.util.SecurityUtil;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentServiceImpl implements DocumentService {
	
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
    
    private final DocumentRepository documentRepo;
    private final DocumentAccessService documentAccessService;
    private final StorageService storageService; // Inject StorageService
    private final com.edu.repositories.EmployerConsentRepository employerConsentRepo;

    public DocumentServiceImpl(DocumentRepository documentRepo,
                               DocumentAccessService documentAccessService,
                               StorageService storageService,
                               com.edu.repositories.EmployerConsentRepository employerConsentRepo) {
        this.documentRepo = documentRepo;
        this.documentAccessService = documentAccessService;
        this.storageService = storageService;
        this.employerConsentRepo = employerConsentRepo;
    }
	
	@Override
	public void upload(MultipartFile file, String documentType) {

	    try {
	        byte[] bytes = file.getBytes();

	        // 1. Hash
	        String sha256 = DigestUtils.sha256Hex(bytes);

	        if (documentRepo.existsBySha256Hash(sha256)) {
	            throw new RuntimeException("Exact duplicate document");
	            
	        }
	        
	        // 2. Upload to Cloudinary
            String secureUrl = storageService.upload(file);

	        // 3. AI analysis (Needs local file? Yes. Create temp file)
            // AI Service expects a File object.
            Path tempFile = Files.createTempFile("edublock-upload", ".pdf");
            Files.write(tempFile, bytes);
            
	        AIResult result = aiService.analyze(tempFile.toFile());
            Files.deleteIfExists(tempFile); // Cleanup

	        SimilarityMatch match = similarityService.findSimilar(
	                result.getPerceptualHash()
	        );

	        Document doc = new Document();
	        doc.setSha256Hash(sha256);
	        doc.setPerceptualHash(result.getPerceptualHash());
	        doc.setFilePath(secureUrl); // Store URL instead of local path
            
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
    public List<DocumentDTO> getMyDocuments() {

        UUID ownerId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();

        if (!"INDIVIDUAL".equals(role)) {
            throw new AccessDeniedException("Only individuals can list own documents");
        }

        return documentRepo.findByOwnerId(ownerId).stream()
            .map(doc -> {
                String fileName = "document.pdf"; // Fallback
                try {
                     // Try to extract filename from URL if possible, or just use ID
                     fileName = Paths.get(doc.getFilePath()).getFileName().toString();
                } catch (Exception e) {}
                
                return new DocumentDTO(
                    doc.getId(),
                    fileName,
                    doc.getStatus(),
                    doc.getCreatedAt(),
                    doc.getFilePath()
                );
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByConsent(UUID consentId, UUID employerId) {
        // 1. Verify consent exists
        com.edu.entity.EmployerConsent consent = employerConsentRepo.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consent not found"));

        // 2. Verify employer owns the consent
        if (!consent.getEmployerId().equals(employerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized access to consent");
        }

        // 3. Verify consent is APPROVED
        if (consent.getStatus() != com.edu.entity.EmployerConsent.Status.APPROVED) {
            throw new org.springframework.security.access.AccessDeniedException("Consent is not approved");
        }

        // 4. Return documents of the owner (individual)
        // Only return APPROVED/VERIFIED documents? Usually yes, but let's return all non-deleted for now OR specific status.
        // Assuming we return all documents the user has.
        return documentRepo.findByOwnerId(consent.getOwnerId()).stream()
            .map(doc -> {
                 String fileName = "document.pdf";
                 try {
                     fileName = java.nio.file.Paths.get(doc.getFilePath()).getFileName().toString();
                 } catch (Exception e) {}
                 
                 return new DocumentDTO(
                     doc.getId(),
                     fileName,
                     doc.getStatus(),
                     doc.getCreatedAt(),
                     doc.getFilePath()
                 );
            })
            .collect(Collectors.toList());
    }
}


