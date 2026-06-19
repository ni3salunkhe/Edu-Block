package com.edu.edublock;

import com.edu.dto.EvidenceSnapshotDTO;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.EvidenceAggregationService;
import com.edu.services.RecommendationExplanationBuilder;
import com.edu.servicesImpl.ReviewerCopilotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ReviewerRecommendationEngineTest {

    private ReviewerRecommendationRepository recommendationRepository;
    private ReviewerFeedbackRepository feedbackRepository;
    private DocumentRepository documentRepository;
    private DocumentVerificationResultRepository verificationResultRepository;
    private InstitutionTrustProfileRepository trustProfileRepository;
    private InstitutionRiskEventRepository riskEventRepository;
    private EvidenceAggregationService evidenceAggregationService;
    private RecommendationExplanationBuilder explanationBuilder;

    private ReviewerCopilotServiceImpl copilotService;

    @BeforeEach
    public void setUp() {
        recommendationRepository = Mockito.mock(ReviewerRecommendationRepository.class);
        feedbackRepository = Mockito.mock(ReviewerFeedbackRepository.class);
        documentRepository = Mockito.mock(DocumentRepository.class);
        verificationResultRepository = Mockito.mock(DocumentVerificationResultRepository.class);
        trustProfileRepository = Mockito.mock(InstitutionTrustProfileRepository.class);
        riskEventRepository = Mockito.mock(InstitutionRiskEventRepository.class);
        evidenceAggregationService = Mockito.mock(EvidenceAggregationService.class);
        explanationBuilder = Mockito.mock(RecommendationExplanationBuilder.class);
        org.springframework.context.ApplicationEventPublisher eventPublisher = Mockito.mock(org.springframework.context.ApplicationEventPublisher.class);

        copilotService = new ReviewerCopilotServiceImpl(
                recommendationRepository,
                feedbackRepository,
                documentRepository,
                verificationResultRepository,
                trustProfileRepository,
                riskEventRepository,
                evidenceAggregationService,
                explanationBuilder,
                eventPublisher
        );
    }

    @Test
    public void testGenerateRecommendationApprove() {
        UUID docId = UUID.randomUUID();
        UUID instId = UUID.randomUUID();

        Document doc = new Document();
        doc.setId(docId);
        doc.setIssuingInstitutionId(instId);
        doc.setOwnerId(UUID.randomUUID());
        doc.setBlockchainTx("0x123abc");

        DocumentVerificationResult dvr = new DocumentVerificationResult();
        dvr.setConfidenceScore(95);
        dvr.setFraudSignals(new ArrayList<>());

        Institution inst = new Institution();
        inst.setId(instId);
        inst.setName("Top University");

        InstitutionTrustProfile trustProfile = new InstitutionTrustProfile();
        trustProfile.setInstitution(inst);
        trustProfile.setTrustScore(95);
        trustProfile.setTrustTier(InstitutionTrustProfile.TrustTier.VERY_HIGH);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(verificationResultRepository.findById(docId)).thenReturn(Optional.of(dvr));
        when(trustProfileRepository.findById(instId)).thenReturn(Optional.of(trustProfile));
        when(riskEventRepository.findByInstitutionIdAndResolvedFalse(instId)).thenReturn(new ArrayList<>());

        EvidenceSnapshotDTO snapshot = new EvidenceSnapshotDTO();
        snapshot.setInstitutionTrustTier("VERY_HIGH");
        snapshot.setOcrWordConfidence(99.0);
        snapshot.setBlockchainAnchored(true);

        when(evidenceAggregationService.aggregateEvidence(docId)).thenReturn(snapshot);
        when(explanationBuilder.buildExplanations(any(), any())).thenReturn(Collections.singletonList("Looks perfect"));
        when(recommendationRepository.findByDocumentIdAndState(any(), any())).thenReturn(Optional.empty());
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewerRecommendation rec = copilotService.generateRecommendation(docId);

        assertNotNull(rec);
        assertEquals(ReviewerRecommendation.RecommendationType.APPROVE, rec.getRecommendation());
        assertEquals(ReviewerRecommendation.RecommendationConfidence.VERY_HIGH, rec.getConfidence());
        assertFalse(rec.getContradictorySignals());
        // 50 + 20 (trust VERY_HIGH) + 15 (verification score 95) + 10 (blockchain) - 0 = 95
        assertEquals(95, rec.getRecommendationScore());
    }

    @Test
    public void testGenerateRecommendationRejectOnFraud() {
        UUID docId = UUID.randomUUID();
        UUID instId = UUID.randomUUID();

        Document doc = new Document();
        doc.setId(docId);
        doc.setIssuingInstitutionId(instId);
        doc.setOwnerId(UUID.randomUUID());

        DocumentVerificationResult dvr = new DocumentVerificationResult();
        dvr.setConfidenceScore(75);
        FraudSignal fs = new FraudSignal();
        fs.setSeverity(FraudSignal.FraudSeverity.CRITICAL);
        fs.setSignalType(FraudSignal.FraudSignalType.DUPLICATE_CERTIFICATE);
        dvr.setFraudSignals(Collections.singletonList(fs));

        Institution inst = new Institution();
        inst.setId(instId);

        InstitutionTrustProfile trustProfile = new InstitutionTrustProfile();
        trustProfile.setInstitution(inst);
        trustProfile.setTrustTier(InstitutionTrustProfile.TrustTier.HIGH);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(verificationResultRepository.findById(docId)).thenReturn(Optional.of(dvr));
        when(trustProfileRepository.findById(instId)).thenReturn(Optional.of(trustProfile));
        when(riskEventRepository.findByInstitutionIdAndResolvedFalse(instId)).thenReturn(new ArrayList<>());

        EvidenceSnapshotDTO snapshot = new EvidenceSnapshotDTO();
        snapshot.setInstitutionTrustTier("HIGH");
        snapshot.setActiveFraudSignals(Collections.singletonList("DUPLICATE_CERTIFICATE (CRITICAL)"));

        when(evidenceAggregationService.aggregateEvidence(docId)).thenReturn(snapshot);
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewerRecommendation rec = copilotService.generateRecommendation(docId);

        assertNotNull(rec);
        assertEquals(ReviewerRecommendation.RecommendationType.REJECT, rec.getRecommendation());
        // 50 + 10 (trust HIGH) + 5 (verification score 75) - 40 (critical fraud) - 0 = 25
        assertEquals(25, rec.getRecommendationScore());
    }

    @Test
    public void testContradictorySignalsDetection() {
        UUID docId = UUID.randomUUID();
        UUID instId = UUID.randomUUID();

        Document doc = new Document();
        doc.setId(docId);
        doc.setIssuingInstitutionId(instId);
        doc.setOwnerId(UUID.randomUUID());

        DocumentVerificationResult dvr = new DocumentVerificationResult();
        dvr.setConfidenceScore(95); // High confidence
        dvr.setFraudSignals(new ArrayList<>());

        Institution inst = new Institution();
        inst.setId(instId);

        InstitutionTrustProfile trustProfile = new InstitutionTrustProfile();
        trustProfile.setInstitution(inst);
        trustProfile.setTrustTier(InstitutionTrustProfile.TrustTier.CRITICAL); // Critical trust institution

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(verificationResultRepository.findById(docId)).thenReturn(Optional.of(dvr));
        when(trustProfileRepository.findById(instId)).thenReturn(Optional.of(trustProfile));
        when(riskEventRepository.findByInstitutionIdAndResolvedFalse(instId)).thenReturn(new ArrayList<>());

        EvidenceSnapshotDTO snapshot = new EvidenceSnapshotDTO();
        snapshot.setInstitutionTrustTier("CRITICAL");

        when(evidenceAggregationService.aggregateEvidence(docId)).thenReturn(snapshot);
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewerRecommendation rec = copilotService.generateRecommendation(docId);

        assertNotNull(rec);
        assertTrue(rec.getContradictorySignals()); // High confidence document from critical trust institution
        assertEquals(ReviewerRecommendation.RecommendationConfidence.LOW, rec.getConfidence());
    }
}
