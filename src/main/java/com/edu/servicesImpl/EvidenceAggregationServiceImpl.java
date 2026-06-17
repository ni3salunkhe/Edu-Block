package com.edu.servicesImpl;

import com.edu.dto.EvidenceSnapshotDTO;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.EvidenceAggregationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EvidenceAggregationServiceImpl implements EvidenceAggregationService {

    private final DocumentRepository documentRepository;
    private final DocumentVerificationResultRepository verificationResultRepository;
    private final DocumentIntelligenceRepository intelligenceRepository;
    private final DocumentOcrRepository ocrRepository;
    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final InstitutionRiskEventRepository riskEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern CONFIDENCE_PATTERN = Pattern.compile("\"averageWordConfidence\"\\s*:\\s*([0-9.]+)");

    public EvidenceAggregationServiceImpl(DocumentRepository documentRepository,
                                         DocumentVerificationResultRepository verificationResultRepository,
                                         DocumentIntelligenceRepository intelligenceRepository,
                                         DocumentOcrRepository ocrRepository,
                                         InstitutionTrustProfileRepository trustProfileRepository,
                                         InstitutionRiskEventRepository riskEventRepository,
                                         UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.intelligenceRepository = intelligenceRepository;
        this.ocrRepository = ocrRepository;
        this.trustProfileRepository = trustProfileRepository;
        this.riskEventRepository = riskEventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceSnapshotDTO aggregateEvidence(UUID documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found for ID: " + documentId));

        DocumentVerificationResult dvr = verificationResultRepository.findById(documentId).orElse(null);
        DocumentIntelligence intel = intelligenceRepository.findById(documentId).orElse(null);
        DocumentOcr ocr = ocrRepository.findById(documentId).orElse(null);

        EvidenceSnapshotDTO dto = new EvidenceSnapshotDTO();
        dto.setSnapshotVersion("SNAPSHOT_V1");
        dto.setOcrTextHash(dvr != null ? dvr.getOcrTextHash() : null);

        // Extract word confidence via Regex from OCR metadata
        double wordConfidence = 100.0;
        if (ocr != null && ocr.getOcrMetadataJson() != null) {
            Matcher m = CONFIDENCE_PATTERN.matcher(ocr.getOcrMetadataJson());
            if (m.find()) {
                try {
                    wordConfidence = Double.parseDouble(m.group(1));
                } catch (Exception ignored) {}
            }
        }
        dto.setOcrWordConfidence(wordConfidence);

        // Owner Name
        String ownerName = userRepository.findById(doc.getOwnerId())
                .map(User::getUsername)
                .orElse("Unknown Owner");
        dto.setOwnerName(ownerName);

        // Resolved Institution Info
        UUID instId = doc.getIssuingInstitutionId();
        dto.setResolvedInstitutionId(instId);

        if (instId != null) {
            InstitutionTrustProfile trustProfile = trustProfileRepository.findById(instId).orElse(null);
            if (trustProfile != null) {
                dto.setInstitutionName(trustProfile.getInstitution().getName());
                dto.setInstitutionTrustScore(trustProfile.getTrustScore());
                dto.setInstitutionTrustTier(trustProfile.getTrustTier().name());
            } else {
                dto.setInstitutionName("Unknown Institution");
                dto.setInstitutionTrustScore(50);
                dto.setInstitutionTrustTier("MEDIUM");
            }

            boolean hasActiveEvents = !riskEventRepository.findByInstitutionIdAndResolvedFalse(instId).isEmpty();
            dto.setHasActiveRiskEvents(hasActiveEvents);
        } else {
            dto.setInstitutionName("Unresolved Institution");
            dto.setInstitutionTrustScore(50);
            dto.setInstitutionTrustTier("MEDIUM");
            dto.setHasActiveRiskEvents(false);
        }

        // Fraud signals
        List<String> fraudSignalsList = new ArrayList<>();
        if (dvr != null && dvr.getFraudSignals() != null) {
            for (FraudSignal fs : dvr.getFraudSignals()) {
                fraudSignalsList.add(fs.getSignalType().name() + " (" + fs.getSeverity().name() + ")");
            }
        }
        dto.setActiveFraudSignals(fraudSignalsList);

        // Verification Explanations from JSON
        List<String> explanations = new ArrayList<>();
        if (dvr != null && dvr.getExplanationsJson() != null) {
            try {
                List<Map<String, Object>> parsedExps = objectMapper.readValue(dvr.getExplanationsJson(), new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> exp : parsedExps) {
                    if (Boolean.TRUE.equals(exp.get("triggered"))) {
                        explanations.add(exp.get("description") + " (" + exp.get("scoreModifier") + ")");
                    }
                }
            } catch (Exception ignored) {}
        }
        dto.setVerificationExplanations(explanations);

        dto.setHasDuplicateSimilarity(doc.getMatchedDocumentId() != null);
        dto.setBlockchainAnchored(doc.getBlockchainTx() != null || (dvr != null && dvr.getIsVerified() && doc.getStatus() == Document.Status.BLOCKCHAIN_ANCHORED));

        return dto;
    }
}
