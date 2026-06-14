package com.edu.edublock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import com.edu.dto.*;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.AuditService;
import com.edu.services.TrustEvaluationService;
import com.edu.servicesImpl.EmployerDocumentServiceImpl;

class EmployerDocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepo;

    @Mock
    private EmployerConsentRepository employerConsentRepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private TrustEvaluationService trustEvaluationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployerDocumentServiceImpl employerDocumentService;

    private UUID employerId;
    private UUID candidateId;
    private UUID docId;
    private UUID instId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employerId = UUID.randomUUID();
        candidateId = UUID.randomUUID();
        docId = UUID.randomUUID();
        instId = UUID.randomUUID();

        User defaultUser = new User();
        defaultUser.setId(candidateId);
        defaultUser.setUsername("john_doe");
        defaultUser.setEmail("john@example.com");
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(defaultUser));
    }

    @Test
    void testSecurity_NoConsent_ThrowsAccessDeniedException() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        // Mock missing consent (returns empty Optional)
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> 
            employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1")
        );
    }

    @Test
    void testSecurity_RevokedConsent_ThrowsAccessDeniedException() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(candidateId);
        consent.setStatus(EmployerConsent.Status.REVOKED);

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.of(consent));

        assertThrows(AccessDeniedException.class, () -> 
            employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1")
        );
    }

    @Test
    void testSecurity_ExpiredConsent_ThrowsAccessDeniedException() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(candidateId);
        consent.setStatus(EmployerConsent.Status.APPROVED);
        consent.setValidUntil(Instant.now().minus(1, ChronoUnit.HOURS)); // Expired 1 hour ago

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.of(consent));

        assertThrows(AccessDeniedException.class, () -> 
            employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1")
        );
    }

    @Test
    void testSecurity_NotYetValidConsent_ThrowsAccessDeniedException() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(candidateId);
        consent.setStatus(EmployerConsent.Status.APPROVED);
        consent.setValidFrom(Instant.now().plus(1, ChronoUnit.HOURS)); // Starts in 1 hour

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.of(consent));

        assertThrows(AccessDeniedException.class, () -> 
            employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1")
        );
    }

    @Test
    void testOwnership_ResolvedFromUserRepository() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        User candidate = new User();
        candidate.setId(candidateId);
        candidate.setUsername("john_doe");
        candidate.setEmail("john@example.com");

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(candidateId);
        consent.setStatus(EmployerConsent.Status.APPROVED);

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        when(userRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.of(consent));

        EmployerDocumentViewDTO details = employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1");

        assertNotNull(details);
        assertEquals("john_doe", details.getOwnerName());
        assertEquals("john@example.com", details.getOwnerEmail());
    }

    @Test
    void testTrust_ExplanationGenerations() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        User candidate = new User();
        candidate.setId(candidateId);

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(candidateId);
        consent.setStatus(EmployerConsent.Status.APPROVED);

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        when(userRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.of(consent));

        // VERY_HIGH
        DocumentTrustResponseDTO veryHighMock = new DocumentTrustResponseDTO();
        veryHighMock.setTrustScore(TrustLevel.VERY_HIGH);
        when(trustEvaluationService.evaluateDocument(any(Document.class), any())).thenReturn(veryHighMock);

        EmployerDocumentViewDTO detailsVeryHigh = employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1");
        assertEquals("Credential issued directly by an accredited institution and anchored on blockchain.", detailsVeryHigh.getVerificationExplanation());

        // HIGH
        DocumentTrustResponseDTO highMock = new DocumentTrustResponseDTO();
        highMock.setTrustScore(TrustLevel.HIGH);
        when(trustEvaluationService.evaluateDocument(any(Document.class), any())).thenReturn(highMock);

        EmployerDocumentViewDTO detailsHigh = employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1");
        assertEquals("Document verified through EduBlock verification workflow and anchored on blockchain.", detailsHigh.getVerificationExplanation());

        // LOW
        DocumentTrustResponseDTO lowMock = new DocumentTrustResponseDTO();
        lowMock.setTrustScore(TrustLevel.LOW);
        when(trustEvaluationService.evaluateDocument(any(Document.class), any())).thenReturn(lowMock);

        EmployerDocumentViewDTO detailsLow = employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1");
        assertEquals("Document exists but lacks sufficient trust signals.", detailsLow.getVerificationExplanation());
    }

    @Test
    void testBadges_RulesAndDeterministicOrdering() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);
        doc.setSource(DocumentSource.INSTITUTION_ISSUED);
        doc.setIssuingInstitutionId(instId);

        User candidate = new User();
        candidate.setId(candidateId);

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(candidateId);
        consent.setStatus(EmployerConsent.Status.APPROVED);

        Institution inst = new Institution();
        inst.setId(instId);
        inst.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        when(userRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.of(consent));
        when(institutionRepository.findById(instId)).thenReturn(Optional.of(inst));

        DocumentTrustResponseDTO trustMetadata = new DocumentTrustResponseDTO();
        trustMetadata.setTrustScore(TrustLevel.VERY_HIGH);
        when(trustEvaluationService.evaluateDocument(any(Document.class), any())).thenReturn(trustMetadata);

        EmployerDocumentViewDTO details = employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1");

        assertNotNull(details.getTrustBadges());
        // Should contain INSTITUTION_ISSUED, ACCREDITED_INSTITUTION, BLOCKCHAIN_VERIFIED
        assertEquals(3, details.getTrustBadges().size());
        
        // Assert exact deterministic order:
        // Order: ACCREDITED_INSTITUTION (0), INSTITUTION_ISSUED (2), BLOCKCHAIN_VERIFIED (4)
        assertEquals(TrustBadgeType.ACCREDITED_INSTITUTION, details.getTrustBadges().get(0));
        assertEquals(TrustBadgeType.INSTITUTION_ISSUED, details.getTrustBadges().get(1));
        assertEquals(TrustBadgeType.BLOCKCHAIN_VERIFIED, details.getTrustBadges().get(2));

        // Test Individual Upload + Anchored -> VERIFIER_APPROVED + BLOCKCHAIN_VERIFIED
        doc.setSource(DocumentSource.INDIVIDUAL_UPLOAD);
        doc.setStatus(Document.Status.BLOCKCHAIN_ANCHORED);
        doc.setIssuingInstitutionId(null);

        details = employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1");
        assertEquals(2, details.getTrustBadges().size());
        assertEquals(TrustBadgeType.VERIFIER_APPROVED, details.getTrustBadges().get(0));
        assertEquals(TrustBadgeType.BLOCKCHAIN_VERIFIED, details.getTrustBadges().get(1));
    }

    @Test
    void testAudit_CreationOnEndpoints() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        User candidate = new User();
        candidate.setId(candidateId);

        EmployerConsent consent = new EmployerConsent();
        consent.setEmployerId(employerId);
        consent.setOwnerId(candidateId);
        consent.setStatus(EmployerConsent.Status.APPROVED);

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        when(userRepository.findById(candidateId)).thenReturn(Optional.of(candidate));
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.of(consent));

        // View shared documents audit verification
        when(employerConsentRepo.findByEmployerId(employerId)).thenReturn(Collections.singletonList(consent));
        when(documentRepo.findByOwnerIdIn(anySet())).thenReturn(Collections.singletonList(doc));
        employerDocumentService.getSharedDocuments(employerId, "127.0.0.1");
        verify(auditService, times(1)).logEmployerAction(employerId, "EMPLOYER_VIEW_SHARED_DOCUMENTS", "EMPLOYER", employerId, null, "127.0.0.1");

        // View document details audit verification
        employerDocumentService.getDocumentDetails(employerId, docId, "127.0.0.1");
        verify(auditService, times(1)).logEmployerAction(employerId, "EMPLOYER_VIEW_DOCUMENT", "DOCUMENT", docId, null, "127.0.0.1");

        // View trust details audit verification
        employerDocumentService.getDocumentTrustDetails(employerId, docId, "127.0.0.1");
        verify(auditService, times(1)).logEmployerAction(employerId, "EMPLOYER_VIEW_TRUST", "DOCUMENT", docId, null, "127.0.0.1");
    }

    @Test
    void testPerformance_BulkLoadingPreloadsResources() {
        EmployerConsent consent1 = new EmployerConsent();
        UUID candidate1 = UUID.randomUUID();
        consent1.setEmployerId(employerId);
        consent1.setOwnerId(candidate1);
        consent1.setStatus(EmployerConsent.Status.APPROVED);

        EmployerConsent consent2 = new EmployerConsent();
        UUID candidate2 = UUID.randomUUID();
        consent2.setEmployerId(employerId);
        consent2.setOwnerId(candidate2);
        consent2.setStatus(EmployerConsent.Status.APPROVED);

        when(employerConsentRepo.findByEmployerId(employerId)).thenReturn(Arrays.asList(consent1, consent2));

        Document d1 = new Document();
        d1.setId(UUID.randomUUID());
        d1.setOwnerId(candidate1);
        d1.setIssuingInstitutionId(instId);

        Document d2 = new Document();
        d2.setId(UUID.randomUUID());
        d2.setOwnerId(candidate2);

        when(documentRepo.findByOwnerIdIn(anySet())).thenReturn(Arrays.asList(d1, d2));

        User u1 = new User();
        u1.setId(candidate1);
        User u2 = new User();
        u2.setId(candidate2);
        when(userRepository.findAllById(anySet())).thenReturn(Arrays.asList(u1, u2));

        Institution inst = new Institution();
        inst.setId(instId);
        when(institutionRepository.findAllById(anySet())).thenReturn(Collections.singletonList(inst));

        employerDocumentService.getSharedDocuments(employerId, "127.0.0.1");

        // Verify bulk loading triggered exactly once (avoiding N+1 queries)
        verify(userRepository, times(1)).findAllById(anySet());
        verify(institutionRepository, times(1)).findAllById(anySet());
        verify(trustEvaluationService, times(1)).evaluateDocuments(anyList());
    }

    @Test
    void testEndpointIsolation_TrustDetailsIndependentlyRevalidatesConsent() {
        Document doc = new Document();
        doc.setId(docId);
        doc.setOwnerId(candidateId);

        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));
        // Mock missing consent specifically for trust lookup
        when(employerConsentRepo.findByEmployerIdAndOwnerId(employerId, candidateId)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> 
            employerDocumentService.getDocumentTrustDetails(employerId, docId, "127.0.0.1")
        );
        // Verify consent lookup was indeed executed
        verify(employerConsentRepo, times(1)).findByEmployerIdAndOwnerId(employerId, candidateId);
    }
}
