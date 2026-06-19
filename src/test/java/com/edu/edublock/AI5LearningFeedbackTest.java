package com.edu.edublock;

import com.edu.dto.events.ReviewerFeedbackSubmittedEvent;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AI5LearningFeedbackTest {

    @Autowired
    private ReviewerLearningService learningService;

    @Autowired
    private FeedbackIntelligenceService feedbackIntelligenceService;

    @Autowired
    private CalibrationEngineService calibrationEngineService;

    @Autowired
    private InstitutionBehavioralLearningService institutionBehavioralLearningService;

    @Autowired
    private VerifierQualityAnalyticsService verifierQualityAnalyticsService;

    @Autowired
    private ReviewerLearningSignalRepository learningSignalRepository;

    @Autowired
    private RulePerformanceMetricRepository rulePerformanceMetricRepository;

    @Autowired
    private RecommendationPerformanceMetricRepository recommendationPerformanceMetricRepository;

    @Autowired
    private CalibrationProposalRepository proposalRepository;

    @Autowired
    private ReviewerFeedbackRepository feedbackRepository;

    @Autowired
    private ReviewerRecommendationRepository recommendationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentVerificationResultRepository verificationResultRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionTrustProfileRepository trustProfileRepository;

    @Autowired
    private VerificationRuleConfigRepository ruleConfigRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Institution testInstitution;
    private Document testDocument;
    private DocumentVerificationResult testDvr;
    private ReviewerRecommendation testRecommendation;
    private ReviewerFeedback testFeedback;

    @BeforeEach
    public void setUp() {
        // Setup base test data
        testUser = new User();
        testUser.setUsername("ai5_test_user_" + UUID.randomUUID().toString().substring(0, 8));
        testUser.setEmail("ai5_test_" + UUID.randomUUID().toString().substring(0, 8) + "@edu.com");
        testUser.setPasswordHash("hash");
        testUser.setRole(User.Role.VERIFIER);
        testUser.setStatus(User.Status.ACTIVE);
        testUser.setCreatedAt(Instant.now());
        testUser = userRepository.save(testUser);

        testInstitution = new Institution();
        testInstitution.setName("AI5 Test University");
        testInstitution.setCode("AI5-" + UUID.randomUUID().toString().substring(0, 8));
        testInstitution.setOfficialEmail("contact@ai5test.edu");
        testInstitution.setTrustLevel(InstitutionTrustLevel.ACCREDITED);
        testInstitution.setStatus(InstitutionStatus.ACTIVE);
        testInstitution = institutionRepository.save(testInstitution);

        InstitutionTrustProfile profile = new InstitutionTrustProfile();
        profile.setInstitutionId(testInstitution.getId());
        profile.setInstitution(testInstitution);
        profile.setTrustScore(85);
        profile.setTrustTier(InstitutionTrustProfile.TrustTier.HIGH);
        profile.setCalculationVersion("AI3_v1");
        profile.setMetricsWindowStart(Instant.now().minus(30, ChronoUnit.DAYS));
        profile.setMetricsWindowEnd(Instant.now());
        profile.setFraudRate(0.0);
        profile.setVerificationSuccessRate(1.0);
        profile.setAverageConfidenceScore(85.0);
        profile.setTotalIssuedCredentials(100L);
        profile.setTotalVerifiedCredentials(100L);
        profile.setTotalFraudSignals(0L);
        profile.setTrustReasons(Collections.singletonList("Accredited institution"));
        profile.setScoreBreakdown(new com.edu.dto.TrustScoreBreakdownDTO(50, 20, 0, 0, 70, Instant.now()));
        profile.setLastCalculatedAt(Instant.now());
        trustProfileRepository.save(profile);

        testDocument = new Document();
        testDocument.setOwnerId(testUser.getId());
        testDocument.setIssuingInstitutionId(testInstitution.getId());
        testDocument.setSha256Hash("ai5_hash_" + UUID.randomUUID().toString());
        testDocument.setDocumentType("DEGREE_CERTIFICATE");
        testDocument.setStatus(Document.Status.UPLOADED);
        testDocument = documentRepository.save(testDocument);

        testDvr = new DocumentVerificationResult();
        testDvr.setDocument(testDocument);
        testDvr.setConfidenceScore(75);
        testDvr.setConfidenceLevel(DocumentVerificationResult.ConfidenceLevel.HIGH);
        testDvr.setRiskLevel(DocumentVerificationResult.RiskLevel.LOW);
        testDvr.setIsVerified(true);
        testDvr.setExplanationsJson("[{\"ruleName\":\"AccreditationRule\",\"scoreModifier\":10,\"description\":\"Accredited\",\"triggered\":true},{\"ruleName\":\"LowTextDensityRule\",\"scoreModifier\":-20,\"description\":\"Low density\",\"triggered\":false}]");
        testDvr.setOcrTextHash("ocrhash555");
        testDvr.setVerificationModelVersion("AI2_v1");
        testDvr.setProcessedAt(Instant.now());
        testDvr = verificationResultRepository.save(testDvr);

        testRecommendation = new ReviewerRecommendation();
        testRecommendation.setDocumentId(testDocument.getId());
        testRecommendation.setGenerationNumber(1);
        testRecommendation.setState(ReviewerRecommendation.RecommendationState.ACTIVE);
        testRecommendation.setRecommendation(ReviewerRecommendation.RecommendationType.REVIEW);
        testRecommendation.setRecommendationScore(55);
        testRecommendation.setScoreBreakdown(new com.edu.dto.RecommendationScoreBreakdownDTO(10, 5, 0, 0, 0));
        testRecommendation.setConfidence(ReviewerRecommendation.RecommendationConfidence.MEDIUM);
        testRecommendation.setContradictorySignals(false);
        testRecommendation.setReviewPriority(45);
        testRecommendation.setGeneratedAt(Instant.now());
        testRecommendation.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        testRecommendation.setModelVersion("AI4_v1");
        testRecommendation.setExplanationSummary(Collections.singletonList("Medium confidence review needed"));
        testRecommendation.setEvidenceSnapshot(new com.edu.dto.EvidenceSnapshotDTO());
        testRecommendation.setOpenedAt(Instant.now().minus(60, ChronoUnit.SECONDS));
        testRecommendation.setCompletedAt(Instant.now());
        testRecommendation = recommendationRepository.save(testRecommendation);

        testFeedback = new ReviewerFeedback();
        testFeedback.setRecommendationId(testRecommendation.getId());
        testFeedback.setVerifierId(testUser.getId());
        testFeedback.setActualDecision("APPROVED");
        testFeedback.setIsOverride(true);
        testFeedback.setDecisionGap(45);
        testFeedback.setComment("Approved because signature is clear physically.");
        testFeedback.setOverrideReason(ReviewerFeedback.OverrideReason.FALSE_POSITIVE_FRAUD);
        testFeedback.setCreatedAt(Instant.now());
        testFeedback = feedbackRepository.save(testFeedback);
    }

    @AfterEach
    public void tearDown() {
        try {
            feedbackRepository.deleteAll();
            recommendationRepository.deleteAll();
            verificationResultRepository.deleteAll();
            documentRepository.deleteAll();
            trustProfileRepository.deleteAll();
            institutionRepository.deleteAll();
            userRepository.deleteAll();
            proposalRepository.deleteAll();
            rulePerformanceMetricRepository.deleteAll();
            recommendationPerformanceMetricRepository.deleteAll();
        } catch (Exception ignored) {}
    }

    @Test
    public void testRuleCodeStability() {
        // Act: Process the feedback to generate a reviewer learning signal
        ReviewerLearningSignal signal = learningService.processFeedbackSignal(testFeedback.getId());

        // Assert: Triggered rule codes are populated based on rule configuration, not explanation text
        assertNotNull(signal);
        assertEquals(testRecommendation.getId(), signal.getRecommendationId());
        assertTrue(signal.getTriggeredRuleCodes().contains("AccreditationRule"));
        assertFalse(signal.getTriggeredRuleCodes().contains("LowTextDensityRule"));
        assertNotNull(signal.getTriggeredRuleCodes());
    }

    @Test
    public void testSnapshotTraceability() {
        ReviewerLearningSignal signal = learningService.processFeedbackSignal(testFeedback.getId());

        // Assert: Engine lineage versions are correctly captured
        assertNotNull(signal);
        assertEquals("AI2_v1", signal.getVerificationEngineVersion());
        assertEquals("AI3_v1", signal.getTrustEngineVersion());
        assertEquals("AI4_v1", signal.getCopilotVersion());
        assertEquals("RULESET_2026_06", signal.getRuleConfigurationVersion());
    }

    @Test
    public void testRecommendationAccuracyAndPerformanceMetrics() {
        // Create baseline learning signals
        ReviewerLearningSignal signal = learningService.processFeedbackSignal(testFeedback.getId());
        assertNotNull(signal);

        Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(1, ChronoUnit.HOURS);

        // Act: Run recommendation metrics aggregation
        List<RecommendationPerformanceMetric> metrics = feedbackIntelligenceService.aggregateRecommendationPerformance(start, end);

        // Assert
        assertNotNull(metrics);
        assertFalse(metrics.isEmpty());

        RecommendationPerformanceMetric reviewMetric = metrics.stream()
                .filter(m -> "REVIEW".equals(m.getRecommendationType()))
                .findFirst()
                .orElse(null);

        assertNotNull(reviewMetric);
        assertEquals(1L, reviewMetric.getTotalRecommendations());
        assertEquals(0L, reviewMetric.getAcceptedRecommendations());
        assertEquals(1L, reviewMetric.getOverriddenRecommendations());
        assertEquals(0.0, reviewMetric.getAcceptanceRate());
        assertEquals(1.0, reviewMetric.getOverrideRate());
    }

    @Test
    public void testDuplicateProposalAndSupersede() {
        // Pre-create rule config for cooldown checking
        VerificationRuleConfig ruleConfig = new VerificationRuleConfig("AccreditationRule", 10, true);
        ruleConfigRepository.save(ruleConfig);

        // Pre-create high-override rule performance metrics
        RulePerformanceMetric metric = new RulePerformanceMetric();
        metric.setRuleName("AccreditationRule");
        metric.setTargetType("RULE");
        metric.setTotalEvaluations(100L);
        metric.setOverrideCount(40L);
        metric.setTruePositives(60L);
        metric.setFalsePositives(40L);
        metric.setFalseNegatives(0L);
        metric.setPrecisionRate(0.60);
        metric.setOverrideRate(0.40);
        metric.setMetricsWindowStart(Instant.now().minus(30, ChronoUnit.DAYS));
        metric.setMetricsWindowEnd(Instant.now());
        rulePerformanceMetricRepository.save(metric);

        // Act: First calibration run
        List<CalibrationProposal> firstRun = calibrationEngineService.evaluateCalibration();
        assertFalse(firstRun.isEmpty());
        assertEquals(1, firstRun.size());
        assertEquals(CalibrationProposal.ProposalStatus.PENDING, firstRun.get(0).getStatus());

        UUID firstProposalId = firstRun.get(0).getId();

        // Act: Second calibration run (should mark previous as SUPERSEDED and create a new PENDING one)
        List<CalibrationProposal> secondRun = calibrationEngineService.evaluateCalibration();
        assertFalse(secondRun.isEmpty());

        CalibrationProposal oldProposal = proposalRepository.findById(firstProposalId).orElse(null);
        assertNotNull(oldProposal);
        assertEquals(CalibrationProposal.ProposalStatus.SUPERSEDED, oldProposal.getStatus());

        long activePendingCount = proposalRepository.findByStatus(CalibrationProposal.ProposalStatus.PENDING).size();
        assertEquals(1, activePendingCount);
    }

    @Test
    public void testCalibrationCooldown() {
        VerificationRuleConfig ruleConfig = new VerificationRuleConfig("AccreditationRule", 10, true);
        ruleConfigRepository.save(ruleConfig);

        RulePerformanceMetric metric = new RulePerformanceMetric();
        metric.setRuleName("AccreditationRule");
        metric.setTargetType("RULE");
        metric.setTotalEvaluations(100L);
        metric.setOverrideCount(40L);
        metric.setTruePositives(60L);
        metric.setFalsePositives(40L);
        metric.setFalseNegatives(0L);
        metric.setPrecisionRate(0.60);
        metric.setOverrideRate(0.40);
        metric.setMetricsWindowStart(Instant.now().minus(30, ChronoUnit.DAYS));
        metric.setMetricsWindowEnd(Instant.now());
        rulePerformanceMetricRepository.save(metric);

        // Act: Generate proposal and apply it
        List<CalibrationProposal> run = calibrationEngineService.evaluateCalibration();
        assertEquals(1, run.size());
        UUID proposalId = run.get(0).getId();

        calibrationEngineService.applyProposal(proposalId, testUser.getId());

        // Assert proposal is APPROVED
        CalibrationProposal applied = proposalRepository.findById(proposalId).orElse(null);
        assertNotNull(applied);
        assertEquals(CalibrationProposal.ProposalStatus.APPROVED, applied.getStatus());
        assertNotNull(applied.getLastCalibrationAt());

        // Act: Run evaluation again immediately. Second proposal must be blocked due to cooldown window
        List<CalibrationProposal> cooldownRun = calibrationEngineService.evaluateCalibration();
        
        // Assert: No new proposal created (since the only trigger AccreditationRule is on cooldown)
        long pendingCount = proposalRepository.findByStatus(CalibrationProposal.ProposalStatus.PENDING).size();
        assertEquals(0, pendingCount);
    }
}
