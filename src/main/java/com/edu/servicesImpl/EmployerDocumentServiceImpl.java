package com.edu.servicesImpl;

import com.edu.dto.DocumentTrustResponseDTO;
import com.edu.dto.EmployerDocumentViewDTO;
import com.edu.dto.TrustBadgeType;
import com.edu.dto.TrustLevel;
import com.edu.dto.TrustSource;
import com.edu.entity.Document;
import com.edu.entity.DocumentSource;
import com.edu.entity.EmployerConsent;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.entity.User;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.EmployerConsentRepository;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.AuditService;
import com.edu.services.EmployerDocumentService;
import com.edu.services.TrustEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployerDocumentServiceImpl implements EmployerDocumentService {

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private EmployerConsentRepository employerConsentRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private TrustEvaluationService trustEvaluationService;

    @Autowired
    private AuditService auditService;

    private void validateConsent(UUID employerId, UUID ownerId) {
        EmployerConsent consent = employerConsentRepo.findByEmployerIdAndOwnerId(employerId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Access denied: Candidate consent is not approved."));

        if (consent.getStatus() != EmployerConsent.Status.APPROVED) {
            throw new AccessDeniedException("Access denied: Candidate consent is not approved.");
        }

        Instant now = Instant.now();
        if (consent.getValidFrom() != null && now.isBefore(consent.getValidFrom())) {
            throw new AccessDeniedException("Access denied: Candidate consent validity has not started.");
        }

        if (consent.getValidUntil() != null && now.isAfter(consent.getValidUntil())) {
            throw new AccessDeniedException("Access denied: Candidate consent has expired.");
        }
    }

    private List<TrustBadgeType> generateBadges(DocumentSource source, Document.Status status, InstitutionTrustLevel trustLevel) {
        List<TrustBadgeType> badges = new ArrayList<>();
        if (source == DocumentSource.INSTITUTION_ISSUED) {
            badges.add(TrustBadgeType.INSTITUTION_ISSUED);
            if (trustLevel == InstitutionTrustLevel.ACCREDITED) {
                badges.add(TrustBadgeType.ACCREDITED_INSTITUTION);
                badges.add(TrustBadgeType.BLOCKCHAIN_VERIFIED);
            } else if (trustLevel == InstitutionTrustLevel.VERIFIED) {
                badges.add(TrustBadgeType.VERIFIED_INSTITUTION);
            }
        } else if (source == DocumentSource.INDIVIDUAL_UPLOAD && status == Document.Status.BLOCKCHAIN_ANCHORED) {
            badges.add(TrustBadgeType.VERIFIER_APPROVED);
            badges.add(TrustBadgeType.BLOCKCHAIN_VERIFIED);
        }
        return badges.stream().sorted().collect(Collectors.toList());
    }

    private TrustSource calculateTrustSource(DocumentSource source, Document.Status status, InstitutionTrustLevel trustLevel) {
        if (source == DocumentSource.INSTITUTION_ISSUED) {
            if (trustLevel == InstitutionTrustLevel.ACCREDITED) {
                return TrustSource.ACCREDITED_INSTITUTION;
            } else if (trustLevel == InstitutionTrustLevel.VERIFIED) {
                return TrustSource.VERIFIED_INSTITUTION;
            }
        } else if (source == DocumentSource.INDIVIDUAL_UPLOAD && status == Document.Status.BLOCKCHAIN_ANCHORED) {
            return TrustSource.VERIFIER_APPROVED;
        }
        return TrustSource.LOW_CONFIDENCE;
    }

    private String calculateVerificationExplanation(TrustLevel trustScore) {
        if (trustScore == TrustLevel.VERY_HIGH) {
            return "Credential issued directly by an accredited institution and anchored on blockchain.";
        } else if (trustScore == TrustLevel.HIGH) {
            return "Document verified through EduBlock verification workflow and anchored on blockchain.";
        } else {
            return "Document exists but lacks sufficient trust signals.";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerDocumentViewDTO> getSharedDocuments(UUID employerId, String ipAddress) {
        // Step 1: Retrieve active consents
        List<EmployerConsent> consents = employerConsentRepo.findByEmployerId(employerId);
        Instant now = Instant.now();

        // Step 2: Filter active/valid consents and collect candidate ownerIds removing duplicates
        Set<UUID> ownerIds = consents.stream()
                .filter(c -> c.getStatus() == EmployerConsent.Status.APPROVED)
                .filter(c -> c.getValidFrom() == null || !now.isBefore(c.getValidFrom()))
                .filter(c -> c.getValidUntil() == null || !now.isAfter(c.getValidUntil()))
                .map(EmployerConsent::getOwnerId)
                .collect(Collectors.toSet());

        if (ownerIds.isEmpty()) {
            auditService.logEmployerAction(employerId, "EMPLOYER_VIEW_SHARED_DOCUMENTS", "EMPLOYER", employerId, null, ipAddress);
            return Collections.emptyList();
        }

        // Step 3: Bulk load documents, users, institutions
        List<Document> documents = documentRepo.findByOwnerIdIn(ownerIds);

        Map<UUID, User> userMap = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Set<UUID> institutionIds = documents.stream()
                .map(Document::getIssuingInstitutionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Institution> institutionMap = new HashMap<>();
        if (!institutionIds.isEmpty()) {
            institutionMap = institutionRepository.findAllById(institutionIds).stream()
                    .collect(Collectors.toMap(Institution::getId, inst -> inst));
        }

        // Preload trust metadata in bulk
        List<DocumentTrustResponseDTO> trustList = trustEvaluationService.evaluateDocuments(documents);
        Map<UUID, DocumentTrustResponseDTO> trustMap = trustList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DocumentTrustResponseDTO::getDocumentId, t -> t));

        // Step 4 & 5: Process DTO creation
        List<EmployerDocumentViewDTO> results = new ArrayList<>();
        for (Document doc : documents) {
            User owner = userMap.get(doc.getOwnerId());
            if (owner == null) continue;

            Institution inst = doc.getIssuingInstitutionId() != null ? institutionMap.get(doc.getIssuingInstitutionId()) : null;
            DocumentTrustResponseDTO trustMetadata = trustMap.get(doc.getId());

            TrustLevel trustScore = trustMetadata != null ? trustMetadata.getTrustScore() : TrustLevel.LOW;
            InstitutionTrustLevel instTrust = inst != null ? inst.getTrustLevel() : null;

            String docName = "Document-" + doc.getId().toString().substring(0, 8).toUpperCase();
            try {
                if (doc.getFilePath() != null) {
                    docName = Paths.get(doc.getFilePath()).getFileName().toString();
                }
            } catch (Exception e) {}

            results.add(new EmployerDocumentViewDTO(
                    doc.getId(),
                    docName,
                    doc.getDocumentType(),
                    owner.getUsername(),
                    owner.getEmail(),
                    trustScore,
                    calculateTrustSource(doc.getSource(), doc.getStatus(), instTrust),
                    doc.getSource(),
                    inst != null ? inst.getName() : null,
                    instTrust,
                    trustMetadata != null && trustMetadata.isInstitutionVerified(),
                    doc.getCredentialReference(),
                    trustMetadata != null && trustMetadata.isBlockchainAnchored(),
                    doc.getBlockchainTx(),
                    calculateVerificationExplanation(trustScore),
                    true,
                    generateBadges(doc.getSource(), doc.getStatus(), instTrust)
            ));
        }

        // Step 6: Audit trail creation
        auditService.logEmployerAction(employerId, "EMPLOYER_VIEW_SHARED_DOCUMENTS", "EMPLOYER", employerId, null, ipAddress);

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerDocumentViewDTO getDocumentDetails(UUID employerId, UUID documentId, String ipAddress) {
        // Step 1: Load document
        Document doc = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Step 2: Resolve owner and validate ownership (never trust copied metadata)
        User owner = userRepository.findById(doc.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Candidate owner not found"));

        // Step 3: Validate consent
        validateConsent(employerId, doc.getOwnerId());

        // Step 4: Load institution if present
        Institution inst = null;
        if (doc.getIssuingInstitutionId() != null) {
            inst = institutionRepository.findById(doc.getIssuingInstitutionId()).orElse(null);
        }

        // Step 5: Evaluate trust
        DocumentTrustResponseDTO trustMetadata = trustEvaluationService.evaluateDocument(doc, inst);
        TrustLevel trustScore = trustMetadata != null ? trustMetadata.getTrustScore() : TrustLevel.LOW;
        InstitutionTrustLevel instTrust = inst != null ? inst.getTrustLevel() : null;

        String docName = "Document-" + doc.getId().toString().substring(0, 8).toUpperCase();
        try {
            if (doc.getFilePath() != null) {
                docName = Paths.get(doc.getFilePath()).getFileName().toString();
            }
        } catch (Exception e) {}

        // Step 6: Generate DTO
        EmployerDocumentViewDTO dto = new EmployerDocumentViewDTO(
                doc.getId(),
                docName,
                doc.getDocumentType(),
                owner.getUsername(),
                owner.getEmail(),
                trustScore,
                calculateTrustSource(doc.getSource(), doc.getStatus(), instTrust),
                doc.getSource(),
                inst != null ? inst.getName() : null,
                instTrust,
                trustMetadata != null && trustMetadata.isInstitutionVerified(),
                doc.getCredentialReference(),
                trustMetadata != null && trustMetadata.isBlockchainAnchored(),
                doc.getBlockchainTx(),
                calculateVerificationExplanation(trustScore),
                true,
                generateBadges(doc.getSource(), doc.getStatus(), instTrust)
        );

        // Step 7: Audit creation
        auditService.logEmployerAction(employerId, "EMPLOYER_VIEW_DOCUMENT", "DOCUMENT", documentId, null, ipAddress);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTrustResponseDTO getDocumentTrustDetails(UUID employerId, UUID documentId, String ipAddress) {
        // Step 1: Load document
        Document doc = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Step 2: Revalidate consent again
        validateConsent(employerId, doc.getOwnerId());

        // Step 3: Load institution
        Institution inst = null;
        if (doc.getIssuingInstitutionId() != null) {
            inst = institutionRepository.findById(doc.getIssuingInstitutionId()).orElse(null);
        }

        // Step 4: Evaluate trust
        DocumentTrustResponseDTO dto = trustEvaluationService.evaluateDocument(doc, inst);

        // Step 5: Audit creation
        auditService.logEmployerAction(employerId, "EMPLOYER_VIEW_TRUST", "DOCUMENT", documentId, null, ipAddress);

        return dto;
    }
}
