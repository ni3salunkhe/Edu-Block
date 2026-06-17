package com.edu.edublock;

import com.edu.dto.events.DocumentEvents.DocumentUploadedEvent;
import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.repositories.DocumentOcrRepository;
import com.edu.repositories.DocumentIntelligenceRepository;
import com.edu.repositories.DocumentRepository;
import com.edu.repositories.DocumentHashRepository;
import com.edu.services.OCRService;
import com.edu.services.ImageHashService;
import com.edu.util.FileDownloadHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import java.io.File;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AsynchronousEventPipelineTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentOcrRepository ocrRepository;

    @Autowired
    private DocumentIntelligenceRepository intelligenceRepository;

    @MockBean
    private OCRService ocrService;

    @MockBean
    private ImageHashService imageHashService;

    @MockBean
    private FileDownloadHelper fileDownloadHelper;

    @Test
    public void testEventPipelineExecution() throws Exception {
        // 1. Create and Save Document
        Document doc = new Document();
        doc.setSha256Hash("dummy-sha256-hash-" + UUID.randomUUID().toString());
        doc.setFilePath("http://cloudinary.mock/doc.pdf");
        doc.setStatus(Document.Status.UPLOADED);
        doc = documentRepository.save(doc);

        UUID docId = doc.getId();

        // 2. Mock File Download & OCR & Hashing
        File mockFile = File.createTempFile("mock_download_", ".png");
        mockFile.deleteOnExit();

        when(fileDownloadHelper.downloadFromUrl(anyString())).thenReturn(mockFile);
        when(ocrService.extractText(any(File.class))).thenReturn(
            "Name: BOB JOHNSON\nEnrollment No: ENR-8899\nCGPA: 8.8\nPassed in: 2025\nDegree: B.Tech\nCertificate No: CERT-556"
        );
        // OpenCV 1024-character binary mock hash
        StringBuilder mockHash = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            mockHash.append(i % 2 == 0 ? "1" : "0");
        }
        when(imageHashService.computePerceptualHash(any(File.class))).thenReturn(mockHash.toString());

        // 3. Publish Document Uploaded Event
        eventPublisher.publishEvent(new DocumentUploadedEvent(docId));

        // 4. Wait for async threads to complete execution (max 5 seconds)
        int attempts = 0;
        DocumentOcr ocr = null;
        DocumentIntelligence intel = null;
        Document updatedDoc = null;

        while (attempts < 25) {
            Thread.sleep(200);
            ocr = ocrRepository.findById(docId).orElse(null);
            intel = intelligenceRepository.findById(docId).orElse(null);
            updatedDoc = documentRepository.findById(docId).orElse(null);

            if (ocr != null && ocr.getOcrStatus() == DocumentOcr.OcrStatus.SUCCESS &&
                intel != null && intel.getProcessingStatus() == DocumentIntelligence.IntelligenceStatus.COMPLETED &&
                updatedDoc != null && updatedDoc.getStatus() != Document.Status.UPLOADED) {
                break;
            }
            attempts++;
        }

        // 5. Assertions
        assertNotNull(ocr, "OCR record should be created");
        assertEquals(DocumentOcr.OcrStatus.SUCCESS, ocr.getOcrStatus(), "OCR failed with reason: " + ocr.getFailureReason());
        assertNotNull(ocr.getRawText(), "Raw OCR text should be stored");

        assertNotNull(intel, "DocumentIntelligence record should be created");
        assertEquals(DocumentIntelligence.IntelligenceStatus.COMPLETED, intel.getProcessingStatus());
        assertEquals("BOB JOHNSON", intel.getStudentName());
        assertEquals("ENR-8899", intel.getEnrollmentNumber());
        assertEquals("B.TECH", intel.getDegreeProgram());
        assertEquals(2025, intel.getGraduationYear());

        assertNotNull(updatedDoc, "Document status should be updated");
        // Status should be set to AI_REVIEW_REQUIRED because InstitutionName is missing/unresolved
        assertEquals(Document.Status.AI_REVIEW_REQUIRED, updatedDoc.getStatus());
    }
}
