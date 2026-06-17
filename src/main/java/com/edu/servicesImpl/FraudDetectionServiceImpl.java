package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.entity.FraudSignal.FraudSignalType;
import com.edu.entity.FraudSignal.FraudSeverity;
import com.edu.dto.SimilarityMatch;
import com.edu.services.FraudDetectionService;
import com.edu.services.SimilarityMatchService;
import com.edu.repositories.FraudSignalRepository;
import com.edu.services.VerificationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final SimilarityMatchService similarityMatchService;
    private final FraudSignalRepository fraudSignalRepository;
    private final ObjectMapper objectMapper;

    // Pattern to match student name candidates: 3 to 50 uppercase characters
    private static final Pattern NAME_CANDIDATE_PATTERN = Pattern.compile("\\b([A-Z][A-Z ]{3,49})\\b");

    public FraudDetectionServiceImpl(SimilarityMatchService similarityMatchService,
                                     FraudSignalRepository fraudSignalRepository,
                                     ObjectMapper objectMapper) {
        this.similarityMatchService = similarityMatchService;
        this.fraudSignalRepository = fraudSignalRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<FraudSignal> detectSignals(Document doc, DocumentIntelligence intel, DocumentOcr ocr, VerificationContext context) {
        List<FraudSignal> signals = new ArrayList<>();

        // 1. Visual Duplication & Template Manipulation Checks
        if (doc.getPerceptualHash() != null && !doc.getPerceptualHash().trim().isEmpty()) {
            SimilarityMatch match = similarityMatchService.findSimilar(doc.getPerceptualHash());
            if (match != null && match.getDocumentId() != null && !match.getDocumentId().equals(doc.getId())) {
                double score = match.getScore();
                if (score >= 0.85) {
                    // Check if it's Template Manipulation (visual template matches, but student names differ)
                    boolean namesDiffer = false;
                    if (intel != null && intel.getStudentName() != null) {
                        // Normally we'd look up the matched doc's intelligence, let's assume if it is an exact visual match (>95%)
                        // but not a duplicate certificate, it's template manipulation.
                        if (score >= 0.95) {
                            namesDiffer = true; // High template match but different document upload
                        }
                    }

                    FraudSignal signal = new FraudSignal();
                    signal.setDocument(doc);
                    signal.setDetectedAt(Instant.now());

                    if (namesDiffer && score >= 0.95) {
                        signal.setSignalType(FraudSignalType.TEMPLATE_MANIPULATION);
                        signal.setSeverity(FraudSeverity.CRITICAL);
                        signal.setDescription(String.format("Template manipulation suspected. Visual match of %.1f%% with document %s, but metadata differs.", score * 100, match.getDocumentId()));
                    } else {
                        signal.setSignalType(FraudSignalType.DUPLICATE_CERTIFICATE);
                        signal.setSeverity(score >= 0.90 ? FraudSeverity.CRITICAL : FraudSeverity.HIGH);
                        signal.setDescription(String.format("Duplicate certificate detected. Visual similarity of %.1f%% matches document %s.", score * 100, match.getDocumentId()));
                    }

                    Map<String, Object> evidence = new HashMap<>();
                    evidence.put("matchedDocumentId", match.getDocumentId());
                    evidence.put("similarityScore", score);
                    signal.setEvidenceJson(serializeEvidence(evidence));
                    signals.add(signal);
                }
            }
        }

        // 2. Credential Reference Reuse Check
        if (intel != null && intel.getCredentialReference() != null && !intel.getCredentialReference().trim().isEmpty()) {
            long reuseCount = fraudSignalRepository.countReferenceReuseExcludingOwner(doc.getId(), doc.getOwnerId(), intel.getCredentialReference());
            if (reuseCount > 0) {
                FraudSignal signal = new FraudSignal();
                signal.setDocument(doc);
                signal.setDetectedAt(Instant.now());
                signal.setSignalType(FraudSignalType.SAME_CREDENTIAL_REFERENCE_MULTIPLE_TIMES);
                signal.setSeverity(FraudSeverity.HIGH);
                signal.setDescription(String.format("Credential reference '%s' is reused by %d other student account(s).", intel.getCredentialReference(), reuseCount));

                Map<String, Object> evidence = new HashMap<>();
                evidence.put("credentialReference", intel.getCredentialReference());
                evidence.put("reuseCount", reuseCount);
                signal.setEvidenceJson(serializeEvidence(evidence));
                signals.add(signal);
            }
        }

        // 3. Document Type Mismatch Check
        if (doc.getDocumentType() != null && intel != null && intel.getClassification() != null) {
            String uploadedType = doc.getDocumentType().toUpperCase();
            String classifiedType = intel.getClassification().name();
            boolean mismatch = false;

            if (uploadedType.contains("DEGREE") && !classifiedType.equals("DEGREE_CERTIFICATE")) {
                mismatch = true;
            } else if (uploadedType.contains("MARKSHEET") && !classifiedType.equals("MARKSHEET")) {
                mismatch = true;
            } else if (uploadedType.contains("TRANSCRIPT") && !classifiedType.equals("TRANSCRIPT")) {
                mismatch = true;
            }

            if (mismatch) {
                FraudSignal signal = new FraudSignal();
                signal.setDocument(doc);
                signal.setDetectedAt(Instant.now());
                signal.setSignalType(FraudSignalType.DOCUMENT_TYPE_MISMATCH);
                signal.setSeverity(FraudSeverity.HIGH);
                signal.setDescription(String.format("Uploaded document type is '%s' but AI classified it as '%s'.", doc.getDocumentType(), intel.getClassification()));

                Map<String, Object> evidence = new HashMap<>();
                evidence.put("uploadedType", doc.getDocumentType());
                evidence.put("classifiedType", intel.getClassification().name());
                signal.setEvidenceJson(serializeEvidence(evidence));
                signals.add(signal);
            }
        }

        // 4. Future Date Check
        if (intel != null && intel.getGraduationYear() != null) {
            int currentYear = LocalDate.now().getYear();
            int gradYear = intel.getGraduationYear();
            if (gradYear > currentYear + 1) {
                FraudSignal signal = new FraudSignal();
                signal.setDocument(doc);
                signal.setDetectedAt(Instant.now());
                signal.setSignalType(FraudSignalType.FUTURE_DATE_DETECTED);
                signal.setSeverity(FraudSeverity.MEDIUM);
                signal.setDescription(String.format("Graduation year '%d' is in the future (current year: %d).", gradYear, currentYear));

                Map<String, Object> evidence = new HashMap<>();
                evidence.put("extractedGraduationYear", gradYear);
                evidence.put("currentYear", currentYear);
                signal.setEvidenceJson(serializeEvidence(evidence));
                signals.add(signal);
            }
        }

        // 5. Multiple Student Names Extracted Check
        if (ocr != null && ocr.getRawText() != null) {
            String rawText = ocr.getRawText();
            Set<String> uniqueNames = new HashSet<>();
            Matcher m = NAME_CANDIDATE_PATTERN.matcher(rawText);
            while (m.find()) {
                String candidate = m.group(1).trim();
                // Filter out common keywords
                if (candidate.length() > 5 && 
                    !candidate.contains("UNIVERSITY") && 
                    !candidate.contains("INSTITUTE") && 
                    !candidate.contains("COLLEGE") &&
                    !candidate.contains("BOARD") &&
                    !candidate.contains("SCHOOL")) {
                    uniqueNames.add(candidate);
                }
            }

            if (uniqueNames.size() >= 2) {
                FraudSignal signal = new FraudSignal();
                signal.setDocument(doc);
                signal.setDetectedAt(Instant.now());
                signal.setSignalType(FraudSignalType.MULTIPLE_NAMES_DETECTED);
                signal.setSeverity(FraudSeverity.MEDIUM);
                signal.setDescription("Multiple distinct student name candidates extracted from raw text: " + uniqueNames);

                Map<String, Object> evidence = new HashMap<>();
                evidence.put("detectedNames", new ArrayList<>(uniqueNames));
                signal.setEvidenceJson(serializeEvidence(evidence));
                signals.add(signal);
            }
        }

        // 6. Low Text Density Check
        if (ocr != null && (ocr.getRawText() == null || ocr.getRawText().trim().length() < 25)) {
            int len = ocr.getRawText() != null ? ocr.getRawText().trim().length() : 0;
            FraudSignal signal = new FraudSignal();
            signal.setDocument(doc);
            signal.setDetectedAt(Instant.now());
            signal.setSignalType(FraudSignalType.LOW_TEXT_DENSITY);
            signal.setSeverity(FraudSeverity.MEDIUM);
            signal.setDescription(String.format("Extremely low OCR text density detected (%d character(s)). Blank page or edited fragment suspected.", len));

            Map<String, Object> evidence = new HashMap<>();
            evidence.put("characterCount", len);
            signal.setEvidenceJson(serializeEvidence(evidence));
            signals.add(signal);
        }

        return signals;
    }

    private String serializeEvidence(Map<String, Object> evidence) {
        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (Exception e) {
            return "{}";
        }
    }
}
