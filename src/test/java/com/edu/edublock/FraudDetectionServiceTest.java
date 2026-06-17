package com.edu.edublock;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.entity.FraudSignal.FraudSignalType;
import com.edu.entity.FraudSignal.FraudSeverity;
import com.edu.dto.SimilarityMatch;
import com.edu.services.SimilarityMatchService;
import com.edu.repositories.FraudSignalRepository;
import com.edu.services.VerificationContext;
import com.edu.servicesImpl.FraudDetectionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class FraudDetectionServiceTest {

    private SimilarityMatchService similarityMatchService;
    private FraudSignalRepository fraudSignalRepository;
    private ObjectMapper objectMapper;
    private VerificationContext mockContext;

    @BeforeEach
    public void setUp() {
        similarityMatchService = Mockito.mock(SimilarityMatchService.class);
        fraudSignalRepository = Mockito.mock(FraudSignalRepository.class);
        objectMapper = new ObjectMapper();
        mockContext = Mockito.mock(VerificationContext.class);
    }

    @Test
    public void testDetectDuplicateCertificate() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setPerceptualHash("someHashValue");

        DocumentIntelligence intel = new DocumentIntelligence();
        DocumentOcr ocr = new DocumentOcr();
        ocr.setRawText("Normal OCR text of considerable length for validation check.");

        // Simulate visual match found in DB
        UUID matchedId = UUID.randomUUID();
        when(similarityMatchService.findSimilar("someHashValue")).thenReturn(new SimilarityMatch(matchedId, 0.92, "Visual match"));

        FraudDetectionServiceImpl service = new FraudDetectionServiceImpl(similarityMatchService, fraudSignalRepository, objectMapper);
        List<FraudSignal> signals = service.detectSignals(doc, intel, ocr, mockContext);

        assertEquals(1, signals.size());
        assertEquals(FraudSignalType.DUPLICATE_CERTIFICATE, signals.get(0).getSignalType());
        assertEquals(FraudSeverity.CRITICAL, signals.get(0).getSeverity());
    }

    @Test
    public void testFutureDateAnomalies() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());

        DocumentIntelligence intel = new DocumentIntelligence();
        // Graduation year is set to 2035 (in the future)
        intel.setGraduationYear(2035);

        DocumentOcr ocr = new DocumentOcr();
        ocr.setRawText("Valid raw text with normal density to prevent low density triggering.");

        FraudDetectionServiceImpl service = new FraudDetectionServiceImpl(similarityMatchService, fraudSignalRepository, objectMapper);
        List<FraudSignal> signals = service.detectSignals(doc, intel, ocr, mockContext);

        assertEquals(1, signals.size());
        assertEquals(FraudSignalType.FUTURE_DATE_DETECTED, signals.get(0).getSignalType());
        assertEquals(FraudSeverity.MEDIUM, signals.get(0).getSeverity());
    }

    @Test
    public void testLowTextDensityAnomaly() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());

        DocumentIntelligence intel = new DocumentIntelligence();
        DocumentOcr ocr = new DocumentOcr();
        ocr.setRawText("Too short"); // Low text density (<25 chars)

        FraudDetectionServiceImpl service = new FraudDetectionServiceImpl(similarityMatchService, fraudSignalRepository, objectMapper);
        List<FraudSignal> signals = service.detectSignals(doc, intel, ocr, mockContext);

        assertEquals(1, signals.size());
        assertEquals(FraudSignalType.LOW_TEXT_DENSITY, signals.get(0).getSignalType());
    }
}
