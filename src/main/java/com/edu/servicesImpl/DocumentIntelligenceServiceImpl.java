package com.edu.servicesImpl;

import com.edu.dto.DocumentIntelligenceDTO;
import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.DocumentIntelligence.IntelligenceStatus;
import com.edu.dto.SimilarityMatch;
import com.edu.repositories.DocumentIntelligenceRepository;
import com.edu.repositories.DocumentOcrRepository;
import com.edu.repositories.DocumentRepository;
import com.edu.services.DocumentIntelligenceService;
import com.edu.services.MetadataExtractor;
import com.edu.services.SimilarityMatchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class DocumentIntelligenceServiceImpl implements DocumentIntelligenceService {

    private final DocumentRepository documentRepository;
    private final DocumentOcrRepository documentOcrRepository;
    private final DocumentIntelligenceRepository intelligenceRepository;
    private final MetadataExtractor metadataExtractor;
    private final SimilarityMatchService similarityService;

    @Value("${edublock.ai.similarity-threshold:0.85}")
    private double similarityThreshold;

    public DocumentIntelligenceServiceImpl(DocumentRepository documentRepository,
                                            DocumentOcrRepository documentOcrRepository,
                                            DocumentIntelligenceRepository intelligenceRepository,
                                            MetadataExtractor metadataExtractor,
                                            SimilarityMatchService similarityService) {
        this.documentRepository = documentRepository;
        this.documentOcrRepository = documentOcrRepository;
        this.intelligenceRepository = intelligenceRepository;
        this.metadataExtractor = metadataExtractor;
        this.similarityService = similarityService;
    }

    @Override
    @Transactional
    public void extractAndValidate(UUID documentId, String rawText) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return;

        long startTime = System.currentTimeMillis();

        DocumentIntelligence intel = intelligenceRepository.findById(documentId)
                .orElseGet(() -> {
                    DocumentIntelligence newIntel = new DocumentIntelligence();
                    newIntel.setDocument(doc);
                    return newIntel;
                });

        intel.setProcessingStatus(DocumentIntelligence.IntelligenceStatus.PROCESSING);
        intelligenceRepository.save(intel);

        try {
            // 1. Classify & Extract Metadata
            metadataExtractor.extractMetadata(rawText, intel);

            // 2. Perform Scalable Visual Similarity Check
            SimilarityMatch match = similarityService.findSimilar(doc.getPerceptualHash());

            // 3. Status Progression Logic
            boolean metadataValid = intel.getClassification() != DocumentIntelligence.DocumentClassification.UNKNOWN 
                    && intel.getStudentName() != null 
                    && intel.getInstitutionName() != null;

            if (match != null && match.getScore() > similarityThreshold) {
                doc.setStatus(Document.Status.AI_FLAGGED);
                doc.setMatchedDocumentId(match.getDocumentId());
                
                // Save audit trail directly in dedicated columns
                intel.setMatchedDocumentId(match.getDocumentId());
                intel.setSimilarityScore(match.getScore());
            } else if (metadataValid) {
                doc.setStatus(Document.Status.AI_VERIFIED);
            } else {
                doc.setStatus(Document.Status.AI_REVIEW_REQUIRED);
            }

            long duration = System.currentTimeMillis() - startTime;
            intel.setProcessingStatus(DocumentIntelligence.IntelligenceStatus.COMPLETED);
            intel.setProcessedAt(Instant.now());
            intel.setAuditMetadataJson(String.format(
                "{\"extractorVersion\":\"1.0.0\",\"classificationReason\":\"Keywords pattern-match\",\"processingDurationMs\":%d}", duration
            ));

            documentRepository.save(doc);
            intelligenceRepository.save(intel);

        } catch (Exception e) {
            // Pipeline Technical Failure Fallback
            intel.setProcessingStatus(DocumentIntelligence.IntelligenceStatus.FAILED);
            intel.setProcessedAt(Instant.now());
            intel.setAuditMetadataJson(String.format("{\"error\":\"%s\"}", e.getMessage()));
            intelligenceRepository.save(intel);

            doc.setStatus(Document.Status.AI_REVIEW_REQUIRED);
            documentRepository.save(doc);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentIntelligenceDTO getIntelligence(UUID documentId) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return null;

        DocumentIntelligence intel = intelligenceRepository.findById(documentId).orElse(null);
        DocumentOcr ocr = documentOcrRepository.findById(documentId).orElse(null);

        // Map lifecycle status and errors
        if (intel != null) {
            boolean processed = intel.getProcessingStatus() == IntelligenceStatus.COMPLETED;
            String status = intel.getProcessingStatus().toString();
            String classification = intel.getClassification() != null ? intel.getClassification().toString() : null;

            return new DocumentIntelligenceDTO(
                intel.getDocumentId(),
                status,
                processed,
                classification,
                intel.getStudentName(),
                intel.getInstitutionName(),
                intel.getEnrollmentNumber(),
                intel.getDegreeProgram(),
                intel.getGraduationYear(),
                intel.getAcademicScores(),
                intel.getCredentialReference(),
                intel.getIssueDate(),
                intel.getProcessedAt(),
                intel.getProcessingStatus() == IntelligenceStatus.FAILED ? "Intelligence processing failed" : null
            );
        }

        // If intelligence doesn't exist yet, check OCR progress or failure
        if (ocr != null) {
            if (ocr.getOcrStatus() == DocumentOcr.OcrStatus.FAILED) {
                return new DocumentIntelligenceDTO(
                    documentId,
                    "FAILED",
                    false,
                    ocr.getFailureReason()
                );
            }
            return new DocumentIntelligenceDTO(
                documentId,
                "PROCESSING",
                false,
                null
            );
        }

        // Default state: PENDING
        return new DocumentIntelligenceDTO(
            documentId,
            "PENDING",
            false,
            null
        );
    }
}
