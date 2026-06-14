package com.edu.edublock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.edu.dto.DocumentTrustResponseDTO;
import com.edu.dto.TrustLevel;
import com.edu.entity.Document;
import com.edu.entity.DocumentSource;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.repositories.InstitutionRepository;
import com.edu.servicesImpl.TrustEvaluationServiceImpl;

class TrustEvaluationServiceImplTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private TrustEvaluationServiceImpl trustEvaluationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRule1_InstitutionIssuedAccredited_VeryHigh() {
        UUID instId = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setDocumentType("Degree");
        doc.setSource(DocumentSource.INSTITUTION_ISSUED);
        doc.setStatus(Document.Status.BLOCKCHAIN_ANCHORED);
        doc.setIssuingInstitutionId(instId);

        Institution inst = new Institution();
        inst.setId(instId);
        inst.setName("Harvard University");
        inst.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        DocumentTrustResponseDTO result = trustEvaluationService.evaluateDocument(doc, inst);

        assertNotNull(result);
        assertEquals(doc.getId(), result.getDocumentId());
        assertEquals("Degree", result.getDocumentType());
        assertEquals(DocumentSource.INSTITUTION_ISSUED, result.getDocumentSource());
        assertEquals(Document.Status.BLOCKCHAIN_ANCHORED, result.getVerificationStatus());
        assertEquals(instId, result.getIssuingInstitutionId());
        assertEquals("Harvard University", result.getIssuingInstitutionName());
        assertEquals(InstitutionTrustLevel.ACCREDITED, result.getInstitutionTrustLevel());
        assertTrue(result.isInstitutionVerified());
        assertTrue(result.isBlockchainAnchored());
        assertEquals(TrustLevel.VERY_HIGH, result.getTrustScore());
    }

    @Test
    void testRule2_IndividualUploadBlockchainAnchored_High() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setDocumentType("Marksheet");
        doc.setSource(DocumentSource.INDIVIDUAL_UPLOAD);
        doc.setStatus(Document.Status.BLOCKCHAIN_ANCHORED);
        doc.setBlockchainTx("0x123abc456def");

        DocumentTrustResponseDTO result = trustEvaluationService.evaluateDocument(doc, null);

        assertNotNull(result);
        assertEquals(DocumentSource.INDIVIDUAL_UPLOAD, result.getDocumentSource());
        assertEquals(Document.Status.BLOCKCHAIN_ANCHORED, result.getVerificationStatus());
        assertTrue(result.isBlockchainAnchored());
        assertEquals("0x123abc456def", result.getTransactionHash());
        assertEquals(TrustLevel.HIGH, result.getTrustScore());
    }

    @Test
    void testRule3_InstitutionIssuedNotAccredited_Low() {
        UUID instId = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setSource(DocumentSource.INSTITUTION_ISSUED);
        doc.setIssuingInstitutionId(instId);

        Institution inst = new Institution();
        inst.setId(instId);
        inst.setName("Registered College");
        inst.setTrustLevel(InstitutionTrustLevel.REGISTERED);

        DocumentTrustResponseDTO result = trustEvaluationService.evaluateDocument(doc, inst);

        assertNotNull(result);
        assertEquals(TrustLevel.LOW, result.getTrustScore());
        assertEquals(InstitutionTrustLevel.REGISTERED, result.getInstitutionTrustLevel());
        assertFalse(result.isInstitutionVerified());
    }

    @Test
    void testRule3_IndividualUploadUnanchored_Low() {
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setSource(DocumentSource.INDIVIDUAL_UPLOAD);
        doc.setStatus(Document.Status.AI_VERIFIED);

        DocumentTrustResponseDTO result = trustEvaluationService.evaluateDocument(doc, null);

        assertNotNull(result);
        assertEquals(TrustLevel.LOW, result.getTrustScore());
        assertFalse(result.isBlockchainAnchored());
    }

    @Test
    void testInstitutionVerified_VerifiedTrustLevel_True() {
        UUID instId = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(UUID.randomUUID());
        doc.setIssuingInstitutionId(instId);

        Institution inst = new Institution();
        inst.setId(instId);
        inst.setTrustLevel(InstitutionTrustLevel.VERIFIED);

        DocumentTrustResponseDTO result = trustEvaluationService.evaluateDocument(doc, inst);

        assertNotNull(result);
        assertEquals(InstitutionTrustLevel.VERIFIED, result.getInstitutionTrustLevel());
        assertTrue(result.isInstitutionVerified());
    }

    @Test
    void testBulkEvaluation_AvoidsNPlus1() {
        UUID instId1 = UUID.randomUUID();
        UUID instId2 = UUID.randomUUID();

        Document doc1 = new Document();
        doc1.setId(UUID.randomUUID());
        doc1.setSource(DocumentSource.INSTITUTION_ISSUED);
        doc1.setIssuingInstitutionId(instId1);

        Document doc2 = new Document();
        doc2.setId(UUID.randomUUID());
        doc2.setSource(DocumentSource.INSTITUTION_ISSUED);
        doc2.setIssuingInstitutionId(instId2);

        Document doc3 = new Document();
        doc3.setId(UUID.randomUUID());
        doc3.setSource(DocumentSource.INDIVIDUAL_UPLOAD); // No institution ID

        Institution inst1 = new Institution();
        inst1.setId(instId1);
        inst1.setName("MIT");
        inst1.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        Institution inst2 = new Institution();
        inst2.setId(instId2);
        inst2.setName("Stanford");
        inst2.setTrustLevel(InstitutionTrustLevel.VERIFIED);

        when(institutionRepository.findAllById(anySet())).thenReturn(Arrays.asList(inst1, inst2));

        List<DocumentTrustResponseDTO> results = trustEvaluationService.evaluateDocuments(Arrays.asList(doc1, doc2, doc3));

        assertNotNull(results);
        assertEquals(3, results.size());

        // Verify MIT evaluated to VERY_HIGH
        assertEquals(TrustLevel.VERY_HIGH, results.get(0).getTrustScore());
        assertEquals("MIT", results.get(0).getIssuingInstitutionName());
        assertTrue(results.get(0).isInstitutionVerified());

        // Verify Stanford evaluated to LOW (VERIFIED but not ACCREDITED)
        assertEquals(TrustLevel.LOW, results.get(1).getTrustScore());
        assertEquals("Stanford", results.get(1).getIssuingInstitutionName());
        assertTrue(results.get(1).isInstitutionVerified());

        // Verify Individual unanchored evaluated to LOW
        assertEquals(TrustLevel.LOW, results.get(2).getTrustScore());

        // Verify repository query was executed EXACTLY ONCE with the collected set of IDs
        verify(institutionRepository, times(1)).findAllById(anySet());
    }
}
