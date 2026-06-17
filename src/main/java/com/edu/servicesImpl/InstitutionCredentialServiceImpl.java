package com.edu.servicesImpl;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.edu.dto.InstitutionIssuedDocumentDTO;
import com.edu.entity.AssociationStatus;
import com.edu.entity.Document;
import com.edu.entity.DocumentSource;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionAssociation;
import com.edu.entity.InstitutionDocumentIssueAudit;
import com.edu.entity.InstitutionStatus;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.entity.InstitutionUser;
import com.edu.entity.User;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.InstitutionAssociationRepository;
import com.edu.repositories.InstitutionDocumentIssueAuditRepository;
import com.edu.repositories.InstitutionUserRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.AuditService;
import com.edu.services.BlockchainService;
import com.edu.services.InstitutionCredentialService;
import com.edu.services.StorageService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationEventPublisher;
import com.edu.dto.events.CredentialEvents.CredentialIssuedEvent;

@Service
public class InstitutionCredentialServiceImpl implements InstitutionCredentialService {

    private final InstitutionUserRepository institutionUserRepo;
    private final InstitutionAssociationRepository associationRepo;
    private final DocumentRepository documentRepo;
    private final InstitutionDocumentIssueAuditRepository issueAuditRepo;
    private final StorageService storageService;
    private final BlockchainService blockchainService;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InstitutionCredentialServiceImpl(InstitutionUserRepository institutionUserRepo,
                                             InstitutionAssociationRepository associationRepo,
                                             DocumentRepository documentRepo,
                                             InstitutionDocumentIssueAuditRepository issueAuditRepo,
                                             StorageService storageService,
                                             BlockchainService blockchainService,
                                             AuditService auditService,
                                             UserRepository userRepository,
                                             ApplicationEventPublisher eventPublisher) {
        this.institutionUserRepo = institutionUserRepo;
        this.associationRepo = associationRepo;
        this.documentRepo = documentRepo;
        this.issueAuditRepo = issueAuditRepo;
        this.storageService = storageService;
        this.blockchainService = blockchainService;
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public InstitutionIssuedDocumentDTO issueDocument(UUID institutionUserId, UUID studentId, UUID associationId,
                                  String documentType, String credentialReference, MultipartFile file) {
        if (credentialReference == null || credentialReference.trim().isEmpty()) {
            throw new IllegalArgumentException("Credential reference cannot be null or empty");
        }
        String trimmedReference = credentialReference.trim();

        // 1 & 2. Resolve and validate mapped institution
        InstitutionUser mapping = institutionUserRepo.findByUserId(institutionUserId)
                .orElseThrow(() -> new AccessDeniedException("User is not mapped to any institution"));
        Institution institution = mapping.getInstitution();

        if (institution.getStatus() != InstitutionStatus.ACTIVE) {
            throw new IllegalArgumentException("Institution must be ACTIVE");
        }
        if (institution.getTrustLevel() != InstitutionTrustLevel.ACCREDITED) {
            throw new IllegalArgumentException("Institution must be ACCREDITED");
        }

        // 3. Resolve and validate student association
        InstitutionAssociation association = associationRepo.findById(associationId)
                .orElseThrow(() -> new RuntimeException("Student association not found"));

        if (!association.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Student ID mismatch");
        }
        if (association.getStatus() != AssociationStatus.APPROVED) {
            throw new IllegalArgumentException("Target student does not have an APPROVED association");
        }
        if (!association.getInstitution().getId().equals(institution.getId())) {
            throw new AccessDeniedException("Association does not belong to the issuing institution");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }

        // Layer 1 validation: file hash duplicate check
        String sha256 = DigestUtils.sha256Hex(bytes);
        if (documentRepo.existsBySha256Hash(sha256)) {
            logRejectAudit(institutionUserId, "Duplicate file hash", studentId, trimmedReference);
            throw new IllegalArgumentException("Exact duplicate document exists");
        }

        // Layer 2 validation: business uniqueness check
        if (documentRepo.existsByIssuingInstitutionIdAndCredentialReferenceAndSource(
                institution.getId(), trimmedReference, DocumentSource.INSTITUTION_ISSUED)) {
            logRejectAudit(institutionUserId, "Duplicate credential reference", studentId, trimmedReference);
            throw new IllegalArgumentException("Duplicate credential reference already exists for this institution");
        }

        // Upload to Cloudinary
        String secureUrl = storageService.upload(file);

        // Save Document (Status UPLOADED to bypass AI/verifier states)
        Document doc = new Document();
        doc.setOwnerId(studentId);
        doc.setDocumentType(documentType);
        doc.setStatus(Document.Status.UPLOADED);
        doc.setSource(DocumentSource.INSTITUTION_ISSUED);
        doc.setIssuingInstitutionId(institution.getId());
        doc.setAssociationId(association.getId());
        doc.setCredentialReference(trimmedReference);
        doc.setFilePath(secureUrl);
        doc.setSha256Hash(sha256);
        doc.setCreatedAt(Instant.now());

        Document saved = documentRepo.save(doc);

        // Blockchain Anchoring
        blockchainService.anchor(saved); // Runtime exception here triggers rollback

        // Persist Audit Entity
        InstitutionDocumentIssueAudit issueAudit = new InstitutionDocumentIssueAudit();
        issueAudit.setDocumentId(saved.getId());
        issueAudit.setInstitutionId(institution.getId());
        issueAudit.setStudentId(studentId);
        issueAudit.setIssuedBy(institutionUserId);
        issueAudit.setCredentialReference(trimmedReference);
        issueAuditRepo.save(issueAudit);

        // System Audit Logging
        String ip = getClientIp();
        String metadata = String.format("{\"documentId\":\"%s\",\"credentialReference\":\"%s\"}", saved.getId(), trimmedReference);
        auditService.logAdminAction(
            institutionUserId,
            "DOCUMENT_ISSUED_BY_INSTITUTION",
            "DOCUMENT",
            saved.getId(),
            metadata,
            ip
        );

        eventPublisher.publishEvent(new CredentialIssuedEvent(institution.getId(), saved.getId()));

        String studentName = userRepository.findById(studentId)
                .map(User::getUsername).orElse("Student");
        return buildIssuedDTO(saved, studentName);
    }

    @Override
    public List<InstitutionIssuedDocumentDTO> getIssuedDocuments(UUID institutionUserId) {
        InstitutionUser mapping = institutionUserRepo.findByUserId(institutionUserId)
                .orElseThrow(() -> new AccessDeniedException("User is not mapped to any institution"));
        List<Document> docs = documentRepo.findByIssuingInstitutionId(mapping.getInstitution().getId());
        if (docs.isEmpty()) return Collections.emptyList();

        // Batch-fetch all student names in a single query (eliminates N+1)
        Set<UUID> ownerIds = docs.stream().map(Document::getOwnerId).collect(Collectors.toSet());
        Map<UUID, String> nameMap = new HashMap<>();
        userRepository.findAllById(ownerIds).forEach(u -> nameMap.put(u.getId(), u.getUsername()));

        return docs.stream()
                .map(doc -> buildIssuedDTO(doc, nameMap.getOrDefault(doc.getOwnerId(), "Student")))
                .collect(Collectors.toList());
    }

    private InstitutionIssuedDocumentDTO buildIssuedDTO(Document doc, String studentName) {
        InstitutionIssuedDocumentDTO dto = new InstitutionIssuedDocumentDTO();
        dto.setDocumentId(doc.getId());
        dto.setStudentId(doc.getOwnerId());
        dto.setStudentName(studentName);
        dto.setDocumentType(doc.getDocumentType());
        dto.setCredentialReference(doc.getCredentialReference());
        dto.setBlockchainAnchored(doc.getStatus() == Document.Status.BLOCKCHAIN_ANCHORED || doc.getBlockchainTx() != null);
        dto.setBlockchainTransactionHash(doc.getBlockchainTx());
        dto.setIssuedAt(doc.getCreatedAt());
        return dto;
    }

    private void logRejectAudit(UUID adminId, String reason, UUID studentId, String credentialReference) {
        String ip = getClientIp();
        String metadata = String.format(
            "{\"reason\":\"%s\",\"studentId\":\"%s\",\"credentialReference\":\"%s\"}",
            reason, studentId, credentialReference
        );
        auditService.logAdminAction(
            adminId,
            "DOCUMENT_ISSUANCE_REJECTED",
            "DOCUMENT",
            UUID.randomUUID(), // placeholder for rejected target
            metadata,
            ip
        );
    }

    private String getClientIp() {
        String ip = "0.0.0.0";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    ip = request.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return ip;
    }
}
