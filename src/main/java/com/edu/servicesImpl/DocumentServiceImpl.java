package com.edu.servicesImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    private com.edu.services.TrustEvaluationService trustEvaluationService;

    @Autowired
    private com.edu.repositories.InstitutionRepository institutionRepository;
    
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
	public void rejectWithComment(UUID id, String comment) {
	    Document doc = documentRepo.findById(id).orElseThrow();

	    // 1. Delete file from Cloudinary (extract public_id from URL)
	    String filePath = doc.getFilePath();
	    if (filePath != null && !filePath.isBlank()) {
	        try {
	            // Extract public_id: everything after /upload/ (and after optional version like v1234/)
	            String publicId = filePath;
	            int uploadIdx = publicId.indexOf("/upload/");
	            if (uploadIdx != -1) {
	                publicId = publicId.substring(uploadIdx + 8); // skip "/upload/"
	                // Remove version segment if present (v1234567890/)
	                if (publicId.matches("v\\d+/.*")) {
	                    publicId = publicId.substring(publicId.indexOf('/') + 1);
	                }
	                // Remove file extension for image resources
	                int dotIdx = publicId.lastIndexOf('.');
	                if (dotIdx != -1) publicId = publicId.substring(0, dotIdx);
	            }
	            // Determine resource type from URL
	            String resourceType = filePath.contains("/raw/upload/") ? "raw" : "image";
	            java.util.Map<String, Object> deleteOptions = com.cloudinary.utils.ObjectUtils.asMap(
	                "resource_type", resourceType
	            );
	            ((CloudinaryStorageServiceImpl) storageService).getCloudinary()
	                .uploader().destroy(publicId, deleteOptions);
	        } catch (Exception e) {
	            // Log but do not block rejection if Cloudinary delete fails
	            System.err.println("Cloudinary delete failed for " + filePath + ": " + e.getMessage());
	        }
	    }

	    // 2. Save rejection details — keep DB record so user sees the reason
	    doc.setStatus(Document.Status.REJECTED);
	    doc.setRejectionComment(comment != null ? comment.trim() : "No reason provided.");
	    doc.setFilePath(null); // File is gone from Cloudinary
	    documentRepo.save(doc);

	    // 3. Audit log
	    try {
	        UUID verifierId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
	        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
	        auditService.logVerifierAction(
	            verifierId, "REJECT_DOCUMENT", "DOCUMENT", id,
	            "{\"sha256\":\"" + doc.getSha256Hash() + "\",\"comment\":\"" + doc.getRejectionComment() + "\"}",
	            request.getRemoteAddr()
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Override
	public void acknowledgeRejection(UUID id) {
	    // Called by the document owner to permanently remove the rejected record
	    Document doc = documentRepo.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
	    if (doc.getStatus() != Document.Status.REJECTED) {
	        throw new RuntimeException("Document is not in REJECTED state");
	    }
	    // Owner check
	    UUID currentUser = SecurityUtil.getCurrentUserId();
	    if (!doc.getOwnerId().equals(currentUser)) {
	        throw new org.springframework.security.access.AccessDeniedException("Not your document");
	    }
	    documentRepo.deleteById(id);
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

        List<Document> docs = documentRepo.findByOwnerId(ownerId);
        List<com.edu.dto.DocumentTrustResponseDTO> trustList = 
                trustEvaluationService.evaluateDocuments(docs);
        
        Map<UUID, com.edu.dto.DocumentTrustResponseDTO> trustMap = trustList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(com.edu.dto.DocumentTrustResponseDTO::getDocumentId, t -> t));

        return docs.stream()
            .map(doc -> {
                String fileName = "Document-" + doc.getId().toString().substring(0, 8).toUpperCase();
                try {
                    if (doc.getFilePath() != null) {
                        fileName = Paths.get(doc.getFilePath()).getFileName().toString();
                    }
                } catch (Exception e) {}

                com.edu.dto.DocumentTrustResponseDTO trust = trustMap.get(doc.getId());

                return new DocumentDTO(
                    doc.getId(),
                    fileName,
                    doc.getStatus(),
                    doc.getCreatedAt(),
                    doc.getFilePath(),
                    doc.getRejectionComment(),
                    trust
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
        List<Document> docs = documentRepo.findByOwnerId(consent.getOwnerId());
        List<com.edu.dto.DocumentTrustResponseDTO> trustList = 
                trustEvaluationService.evaluateDocuments(docs);
        
        Map<UUID, com.edu.dto.DocumentTrustResponseDTO> trustMap = trustList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(com.edu.dto.DocumentTrustResponseDTO::getDocumentId, t -> t));

        return docs.stream()
            .map(doc -> {
                 String fileName = "document.pdf";
                 try {
                     fileName = java.nio.file.Paths.get(doc.getFilePath()).getFileName().toString();
                 } catch (Exception e) {}
                 
                 com.edu.dto.DocumentTrustResponseDTO trust = trustMap.get(doc.getId());

                 return new DocumentDTO(
                     doc.getId(),
                     fileName,
                     doc.getStatus(),
                     doc.getCreatedAt(),
                     doc.getFilePath(),
                     doc.getRejectionComment(),
                     trust
                 );
            })
            .collect(Collectors.toList());
    }

    @Override
    public DocumentDTO getDocumentDTO(UUID documentId) {
        Document doc = getDocumentById(documentId); // Asserts access internally
        
        com.edu.entity.Institution institution = null;
        if (doc.getIssuingInstitutionId() != null) {
            institution = institutionRepository.findById(doc.getIssuingInstitutionId()).orElse(null);
        }
        
        com.edu.dto.DocumentTrustResponseDTO trustMetadata = 
                trustEvaluationService.evaluateDocument(doc, institution);

        String fileName = "Document-" + doc.getId().toString().substring(0, 8).toUpperCase();
        try {
            if (doc.getFilePath() != null) {
                fileName = Paths.get(doc.getFilePath()).getFileName().toString();
            }
        } catch (Exception e) {}

        return new DocumentDTO(
            doc.getId(),
            fileName,
            doc.getStatus(),
            doc.getCreatedAt(),
            doc.getFilePath(),
            doc.getRejectionComment(),
            trustMetadata
        );
    }
}


