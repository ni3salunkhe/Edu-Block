package com.edu.edublock;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.ReviewerCopilotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReviewerCopilotIntegrationTest {

    @Autowired
    private ReviewerCopilotService copilotService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentVerificationResultRepository verificationResultRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionTrustProfileRepository trustProfileRepository;

    @Autowired
    private ReviewerRecommendationRepository recommendationRepository;

    @Autowired
    private ReviewerFeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCopilotRecommendationLineageAndFeedbackFlow() {
        // 1. Create and Save Institution & Trust Profile
        Institution inst = new Institution();
        inst.setName("Integration Test University");
        inst.setCode("INT-UNI-" + UUID.randomUUID().toString().substring(0, 8));
        inst.setOfficialEmail("contact@" + UUID.randomUUID().toString().substring(0, 8) + ".edu");
        inst.setTrustLevel(InstitutionTrustLevel.ACCREDITED);
        inst.setStatus(InstitutionStatus.ACTIVE);
        inst = institutionRepository.save(inst);

        UUID instId = inst.getId();

        InstitutionTrustProfile trustProfile = new InstitutionTrustProfile();
        trustProfile.setInstitutionId(instId);
        trustProfile.setInstitution(inst);
        trustProfile.setTrustScore(95);
        trustProfile.setTrustTier(InstitutionTrustProfile.TrustTier.VERY_HIGH);
        trustProfile.setCalculationVersion("AI3_v1");
        trustProfile.setMetricsWindowStart(Instant.now().minus(30, ChronoUnit.DAYS));
        trustProfile.setMetricsWindowEnd(Instant.now());
        trustProfile.setFraudRate(0.0);
        trustProfile.setVerificationSuccessRate(1.0);
        trustProfile.setAverageConfidenceScore(95.0);
        trustProfile.setTotalIssuedCredentials(10L);
        trustProfile.setTotalVerifiedCredentials(10L);
        trustProfile.setTotalFraudSignals(0L);
        trustProfile.setTrustReasons(new ArrayList<>());
        trustProfile.setScoreBreakdown(new com.edu.dto.TrustScoreBreakdownDTO(60, 25, 0, 0, 85, Instant.now()));
        trustProfile.setLastCalculatedAt(Instant.now());
        trustProfileRepository.save(trustProfile);

        // 2. Create User (Owner) & Document
        User owner = new User();
        owner.setUsername("integrationstudent_" + UUID.randomUUID().toString().substring(0, 8));
        owner.setEmail("student_" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com");
        owner.setPasswordHash("encodedPassword");
        owner.setRole(User.Role.INDIVIDUAL);
        owner.setStatus(User.Status.ACTIVE);
        owner.setCreatedAt(Instant.now());
        owner = userRepository.save(owner);

        Document doc = new Document();
        doc.setOwnerId(owner.getId());
        doc.setIssuingInstitutionId(instId);
        doc.setSha256Hash("hash_" + UUID.randomUUID().toString());
        doc.setDocumentType("DEGREE_CERTIFICATE");
        doc.setStatus(Document.Status.UPLOADED);
        doc.setBlockchainTx("0x123abc");
        doc = documentRepository.save(doc);

        UUID docId = doc.getId();

        // 3. Create Document Verification Result
        DocumentVerificationResult dvr = new DocumentVerificationResult();
        dvr.setDocument(doc);
        dvr.setConfidenceScore(95);
        dvr.setConfidenceLevel(DocumentVerificationResult.ConfidenceLevel.VERY_HIGH);
        dvr.setRiskLevel(DocumentVerificationResult.RiskLevel.LOW);
        dvr.setIsVerified(true);
        dvr.setExplanationsJson("[]");
        dvr.setOcrTextHash("ocrhash123");
        dvr.setVerificationModelVersion("AI2_v1");
        dvr.setProcessedAt(Instant.now());
        verificationResultRepository.save(dvr);

        try {
            // 4. Generate Recommendation (V1)
            ReviewerRecommendation rec = copilotService.generateRecommendation(docId);
            assertNotNull(rec);
            assertEquals(ReviewerRecommendation.RecommendationState.ACTIVE, rec.getState());
            assertEquals(ReviewerRecommendation.RecommendationType.APPROVE, rec.getRecommendation());
            assertEquals(1, rec.getGenerationNumber());

            // 5. Test Throttling (Refreshing immediately should throw IllegalStateException)
            assertThrows(IllegalStateException.class, () -> {
                copilotService.refreshRecommendation(docId);
            });

            // 6. Test Feedback quality validations (Override should fail without reason or with short comment)
            UUID verifierId = UUID.randomUUID(); // Mock verifier id

            // Override Approve -> Reject
            assertThrows(IllegalArgumentException.class, () -> {
                copilotService.saveFeedback(rec.getId(), verifierId, "REJECTED", "too short", "FALSE_POSITIVE_FRAUD");
            });

            assertThrows(IllegalArgumentException.class, () -> {
                copilotService.saveFeedback(rec.getId(), verifierId, "REJECTED", "this comment is long enough but override reason is missing", null);
            });

            // Valid override feedback
            ReviewerFeedback feedback = copilotService.saveFeedback(
                    rec.getId(),
                    verifierId,
                    "REJECTED",
                    "Overriding approval because the physical seal format is completely incorrect on this document.",
                    "ALTERNATIVE_VALID_FORMAT"
            );
            assertNotNull(feedback);
            assertTrue(feedback.getIsOverride());
            assertEquals("REJECTED", feedback.getActualDecision());

        } finally {
            // Cleanup
            try {
                feedbackRepository.deleteAll();
                recommendationRepository.deleteAll();
                verificationResultRepository.deleteById(docId);
                documentRepository.deleteById(docId);
                userRepository.deleteById(owner.getId());
                trustProfileRepository.deleteById(instId);
                institutionRepository.deleteById(instId);
            } catch (Exception ignored) {}
        }
    }
}
