package com.edu.servicesImpl;

import com.edu.entity.*;
import com.edu.entity.DocumentVerificationResult.ConfidenceLevel;
import com.edu.entity.DocumentVerificationResult.RiskLevel;
import com.edu.entity.FraudSignal.FraudSeverity;
import com.edu.repositories.DocumentOcrRepository;
import com.edu.repositories.DocumentIntelligenceRepository;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.DocumentVerificationResultRepository;
import com.edu.repositories.FraudSignalRepository;
import com.edu.services.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class DocumentVerificationServiceImpl implements DocumentVerificationService {

    private final DocumentRepository documentRepository;
    private final DocumentIntelligenceRepository intelligenceRepository;
    private final DocumentOcrRepository ocrRepository;
    private final DocumentVerificationResultRepository verificationResultRepository;
    private final FraudSignalRepository fraudSignalRepository;
    
    private final InstitutionResolver institutionResolver;
    private final FraudDetectionService fraudDetectionService;
    private final VerificationRuleEngine verificationRuleEngine;

    private static final String MODEL_VERSION = "AI2_v1";

    public DocumentVerificationServiceImpl(DocumentRepository documentRepository,
                                           DocumentIntelligenceRepository intelligenceRepository,
                                           DocumentOcrRepository ocrRepository,
                                           DocumentVerificationResultRepository verificationResultRepository,
                                           FraudSignalRepository fraudSignalRepository,
                                           InstitutionResolver institutionResolver,
                                           FraudDetectionService fraudDetectionService,
                                           VerificationRuleEngine verificationRuleEngine) {
        this.documentRepository = documentRepository;
        this.intelligenceRepository = intelligenceRepository;
        this.ocrRepository = ocrRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.fraudSignalRepository = fraudSignalRepository;
        this.institutionResolver = institutionResolver;
        this.fraudDetectionService = fraudDetectionService;
        this.verificationRuleEngine = verificationRuleEngine;
    }

    @Override
    @Transactional
    public void verifyDocument(UUID documentId) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return;

        DocumentIntelligence intel = intelligenceRepository.findById(documentId).orElse(null);
        DocumentOcr ocr = ocrRepository.findById(documentId).orElse(null);

        // Pre-requisites validation
        if (ocr == null || ocr.getOcrStatus() != DocumentOcr.OcrStatus.SUCCESS || ocr.getRawText() == null) {
            markAsReviewRequired(doc);
            return;
        }

        // 1. Resolve Institution
        Institution resolvedInstitution = institutionResolver.resolve(ocr.getRawText()).orElse(null);

        // 2. Resolve Environment/Context Lookups
        long reuseCount = 0;
        if (intel != null && intel.getCredentialReference() != null && !intel.getCredentialReference().trim().isEmpty()) {
            reuseCount = fraudSignalRepository.countReferenceReuseExcludingOwner(doc.getId(), doc.getOwnerId(), intel.getCredentialReference());
        }
        boolean accredited = resolvedInstitution != null && resolvedInstitution.getTrustLevel() == InstitutionTrustLevel.ACCREDITED;

        VerificationContext context = new VerificationContextImpl(resolvedInstitution, reuseCount, accredited);

        // 3. Detect Fraud Signals
        List<FraudSignal> activeSignals = fraudDetectionService.detectSignals(doc, intel, ocr, context);

        // 4. Run Rule Engine (Starting from baseline 50)
        VerificationRuleEngine.EngineResult engineResult = verificationRuleEngine.evaluateRules(doc, intel, ocr, activeSignals, context);
        int finalScore = engineResult.getFinalScore();

        // 5. Determine Confidence Level
        ConfidenceLevel confidenceLevel = determineConfidenceLevel(finalScore);

        // 6. Determine Risk Level
        RiskLevel riskLevel = determineRiskLevel(finalScore, activeSignals);

        // 7. Calculate isVerified Status (Needs score >= 75 and RiskLevel LOW/MEDIUM)
        boolean isVerified = finalScore >= 75 && (riskLevel == RiskLevel.LOW || riskLevel == RiskLevel.MEDIUM);

        // 8. Generate SHA-256 hash of verified OCR text snapshot
        String ocrTextHash = DigestUtils.sha256Hex(ocr.getRawText());

        // 9. Persist DocumentVerificationResult
        DocumentVerificationResult verificationResult = verificationResultRepository.findById(documentId)
                .orElseGet(() -> {
                    DocumentVerificationResult dvr = new DocumentVerificationResult();
                    dvr.setDocument(doc);
                    return dvr;
                });

        verificationResult.setConfidenceScore(finalScore);
        verificationResult.setConfidenceLevel(confidenceLevel);
        verificationResult.setRiskLevel(riskLevel);
        verificationResult.setIsVerified(isVerified);
        verificationResult.setExplanationsJson(engineResult.getExplanationsJson());
        verificationResult.setOcrTextHash(ocrTextHash);
        verificationResult.setVerificationModelVersion(MODEL_VERSION);
        verificationResult.setProcessedAt(Instant.now());

        // Set fraud signals relation
        for (FraudSignal fs : activeSignals) {
            fs.setDocument(doc);
        }
        verificationResult.setFraudSignals(activeSignals);

        // Save result and cascaded fraud signals
        verificationResultRepository.save(verificationResult);

        // 10. Update Document LifeCycle Status
        if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH) {
            doc.setStatus(Document.Status.AI_FLAGGED);
        } else if (isVerified) {
            doc.setStatus(Document.Status.AI_VERIFIED);
        } else {
            doc.setStatus(Document.Status.AI_REVIEW_REQUIRED);
        }
        documentRepository.save(doc);
    }

    private void markAsReviewRequired(Document doc) {
        doc.setStatus(Document.Status.AI_REVIEW_REQUIRED);
        documentRepository.save(doc);
    }

    private ConfidenceLevel determineConfidenceLevel(int score) {
        if (score >= 85) return ConfidenceLevel.VERY_HIGH;
        if (score >= 70) return ConfidenceLevel.HIGH;
        if (score >= 50) return ConfidenceLevel.MEDIUM;
        return ConfidenceLevel.LOW;
    }

    private RiskLevel determineRiskLevel(int score, List<FraudSignal> signals) {
        // Find maximum severity signal
        boolean hasCritical = signals.stream().anyMatch(fs -> fs.getSeverity() == FraudSeverity.CRITICAL);
        boolean hasHigh = signals.stream().anyMatch(fs -> fs.getSeverity() == FraudSeverity.HIGH);
        boolean hasMedium = signals.stream().anyMatch(fs -> fs.getSeverity() == FraudSeverity.MEDIUM);

        if (hasCritical || score < 20) {
            return RiskLevel.CRITICAL;
        }
        if (hasHigh || score <= 40) {
            return RiskLevel.HIGH;
        }
        if (hasMedium || score <= 70) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
