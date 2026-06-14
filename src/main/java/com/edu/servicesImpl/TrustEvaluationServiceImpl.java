package com.edu.servicesImpl;

import com.edu.dto.DocumentTrustResponseDTO;
import com.edu.dto.TrustLevel;
import com.edu.entity.Document;
import com.edu.entity.DocumentSource;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.repositories.InstitutionRepository;
import com.edu.services.TrustEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrustEvaluationServiceImpl implements TrustEvaluationService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Override
    public DocumentTrustResponseDTO evaluateDocument(Document document, Institution institution) {
        if (document == null) {
            return null;
        }

        // 1. Gather basic document fields
        UUID docId = document.getId();
        String docType = document.getDocumentType();
        DocumentSource source = document.getSource();
        Document.Status status = document.getStatus();
        UUID institutionId = document.getIssuingInstitutionId();
        String credentialRef = document.getCredentialReference();

        // 2. Resolve blockchain fields
        String txHash = document.getBlockchainTx();
        boolean blockchainAnchored = txHash != null || status == Document.Status.BLOCKCHAIN_ANCHORED;

        // 3. Resolve institution information
        String instName = null;
        InstitutionTrustLevel instTrustLevel = null;
        boolean institutionVerified = false;

        if (institution != null && institutionId != null && institution.getId().equals(institutionId)) {
            instName = institution.getName();
            instTrustLevel = institution.getTrustLevel();
            institutionVerified = instTrustLevel == InstitutionTrustLevel.VERIFIED 
                    || instTrustLevel == InstitutionTrustLevel.ACCREDITED;
        }

        // 4. Calculate Trust Score
        TrustLevel trustScore = TrustLevel.LOW;

        if (source == DocumentSource.INSTITUTION_ISSUED 
                && instTrustLevel == InstitutionTrustLevel.ACCREDITED) {
            trustScore = TrustLevel.VERY_HIGH;
        } else if (source == DocumentSource.INDIVIDUAL_UPLOAD 
                && status == Document.Status.BLOCKCHAIN_ANCHORED) {
            trustScore = TrustLevel.HIGH;
        }

        return new DocumentTrustResponseDTO(
                docId,
                docType,
                source,
                status,
                institutionId,
                instName,
                instTrustLevel,
                institutionVerified,
                credentialRef,
                blockchainAnchored,
                txHash,
                trustScore
        );
    }

    @Override
    public List<DocumentTrustResponseDTO> evaluateDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect unique non-null institution IDs
        Set<UUID> institutionIds = documents.stream()
                .map(Document::getIssuingInstitutionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Batch pre-fetch institutions to avoid N+1 queries
        Map<UUID, Institution> institutionMap = new HashMap<>();
        if (!institutionIds.isEmpty()) {
            List<Institution> institutions = institutionRepository.findAllById(institutionIds);
            for (Institution inst : institutions) {
                if (inst != null && inst.getId() != null) {
                    institutionMap.put(inst.getId(), inst);
                }
            }
        }

        // Evaluate all documents in-memory
        List<DocumentTrustResponseDTO> results = new ArrayList<>(documents.size());
        for (Document doc : documents) {
            Institution inst = null;
            if (doc.getIssuingInstitutionId() != null) {
                inst = institutionMap.get(doc.getIssuingInstitutionId());
            }
            results.add(evaluateDocument(doc, inst));
        }

        return results;
    }
}
