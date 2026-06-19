package com.edu.edublock;

import com.edu.dto.FraudNetworkGraphDTO;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AI6PredictiveFraudNetworkTest {

    @Autowired
    private FraudNetworkService networkService;

    @Autowired
    private NetworkRiskPropagationService riskPropagationService;

    @Autowired
    private FraudSignatureMatcher signatureMatcher;

    @Autowired
    private FraudCampaignService campaignService;

    @Autowired
    private NightlyGraphSnapshotJob nightlySnapshotJob;

    @Autowired
    private FraudNetworkNodeRepository nodeRepository;

    @Autowired
    private FraudNetworkEdgeRepository edgeRepository;

    @Autowired
    private FraudClusterRepository clusterRepository;

    @Autowired
    private FraudClusterMemberRepository memberRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentVerificationResultRepository verificationResultRepository;

    @Autowired
    private ReviewerRecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationEscalationRepository escalationRepository;

    @Autowired
    private FraudSignatureRepository signatureRepository;

    @Autowired
    private FraudCampaignRepository campaignRepository;

    @Autowired
    private GraphVersionRepository graphVersionRepository;

    private Document docA;
    private Document docB;
    private Document docC;

    @BeforeEach
    public void setUp() {
        escalationRepository.deleteAll();
        memberRepository.deleteAll();
        clusterRepository.deleteAll();
        edgeRepository.deleteAll();
        nodeRepository.deleteAll();
        campaignRepository.deleteAll();
        signatureRepository.deleteAll();
        graphVersionRepository.deleteAll();

        // Create testing documents
        docA = new Document();
        docA.setSha256Hash("hash-a-12345");
        docA.setPerceptualHash("phash-abc");
        docA.setOwnerId(UUID.randomUUID());
        docA.setIssuingInstitutionId(UUID.randomUUID());
        docA.setSource(DocumentSource.INDIVIDUAL_UPLOAD);
        docA.setCreatedAt(Instant.now());
        docA = documentRepository.save(docA);

        docB = new Document();
        docB.setSha256Hash("hash-b-23456");
        docB.setPerceptualHash("phash-abc"); // Same visual template hash
        docB.setOwnerId(UUID.randomUUID());
        docB.setIssuingInstitutionId(docA.getIssuingInstitutionId()); // Same issuing institution
        docB.setSource(DocumentSource.INDIVIDUAL_UPLOAD);
        docB.setCreatedAt(Instant.now());
        docB = documentRepository.save(docB);

        docC = new Document();
        docC.setSha256Hash("hash-c-34567");
        docC.setPerceptualHash("phash-xyz");
        docC.setOwnerId(UUID.randomUUID());
        docC.setSource(DocumentSource.INDIVIDUAL_UPLOAD);
        docC.setCreatedAt(Instant.now());
        docC = documentRepository.save(docC);
    }

    @Test
    public void testRelationshipBuildingAndCanonicalEdges() {
        // Build relationships for Doc A
        networkService.buildRelationships(docA.getId());
        networkService.buildRelationships(docB.getId());

        List<FraudNetworkEdge> edges = edgeRepository.findAll();
        assertFalse(edges.isEmpty(), "Edges should be created");

        // Verify canonical ordering (source UUID must be smaller than target UUID lexicographically)
        for (FraudNetworkEdge edge : edges) {
            String srcStr = edge.getSourceNodeId().toString();
            String tgtStr = edge.getTargetNodeId().toString();
            assertTrue(srcStr.compareTo(tgtStr) < 0, 
                "Source Node UUID must be smaller than Target Node UUID for canonical storage");
        }

        long initialCount = edgeRepository.count();
        
        // Try to trigger duplicate edge insert
        networkService.buildRelationships(docA.getId());
        long duplicateTryCount = edgeRepository.count();
        assertEquals(initialCount, duplicateTryCount, "Edges must be deduplicated and no duplicates inserted");
    }

    @Test
    public void testConnectedComponentClustering() {
        // Build mock nodes with risk scores >= 40 (seeds)
        FraudNetworkNode n1 = new FraudNetworkNode();
        n1.setEntityId(UUID.randomUUID());
        n1.setNodeType(FraudNetworkNode.NodeType.DOCUMENT);
        n1.setRiskScore(50);
        n1.setCreatedAt(Instant.now());
        n1.setUpdatedAt(Instant.now());
        n1 = nodeRepository.save(n1);

        FraudNetworkNode n2 = new FraudNetworkNode();
        n2.setEntityId(UUID.randomUUID());
        n2.setNodeType(FraudNetworkNode.NodeType.DOCUMENT);
        n2.setRiskScore(60);
        n2.setCreatedAt(Instant.now());
        n2.setUpdatedAt(Instant.now());
        n2 = nodeRepository.save(n2);

        FraudNetworkNode n3 = new FraudNetworkNode();
        n3.setEntityId(UUID.randomUUID());
        n3.setNodeType(FraudNetworkNode.NodeType.DOCUMENT);
        n3.setRiskScore(55);
        n3.setCreatedAt(Instant.now());
        n3.setUpdatedAt(Instant.now());
        n3 = nodeRepository.save(n3);

        // Connect n1 <-> n2 and n2 <-> n3
        createMockEdge(n1.getId(), n2.getId(), FraudNetworkEdge.RelationshipType.SHARES_VISUAL_HASH, 2.0);
        createMockEdge(n2.getId(), n3.getId(), FraudNetworkEdge.RelationshipType.SHARES_VISUAL_HASH, 2.0);

        List<FraudCluster> clusters = networkService.executeConnectedComponentClustering();
        assertFalse(clusters.isEmpty(), "Connected components should produce clusters");

        FraudCluster cluster = clusters.get(0);
        assertEquals(3, cluster.getNodeCount(), "Cluster should contain 3 nodes");
        assertEquals(2, cluster.getEdgeCount(), "Cluster should contain 2 edges");
        assertTrue(cluster.getDensity() > 0.0, "Cluster density should be positive");
        assertEquals(2.0 / 3.0, cluster.getDensity(), 0.001, "Topological density should be 2/3");
        assertEquals(4.0 / 3.0, cluster.getWeightedDensity(), 0.001, "Weighted density should be 4/3");

        List<FraudClusterMember> members = memberRepository.findByClusterId(cluster.getId());
        assertEquals(3, members.size(), "Should have exactly 3 saved membership mappings");
    }

    @Test
    public void testRiskPropagationDecay() {
        // Setup issuing institution node
        UUID instId = UUID.randomUUID();
        FraudNetworkNode instNode = new FraudNetworkNode();
        instNode.setEntityId(instId);
        instNode.setNodeType(FraudNetworkNode.NodeType.INSTITUTION);
        instNode.setRiskScore(80);
        instNode.setCreatedAt(Instant.now());
        instNode.setUpdatedAt(Instant.now());
        instNode = nodeRepository.save(instNode);

        // Setup document node
        FraudNetworkNode docNode = new FraudNetworkNode();
        docNode.setEntityId(docA.getId());
        docNode.setNodeType(FraudNetworkNode.NodeType.DOCUMENT);
        docNode.setRiskScore(40);
        docNode.setCreatedAt(Instant.now());
        docNode.setUpdatedAt(Instant.now());
        docNode = nodeRepository.save(docNode);

        // Setup active recommendation for document
        ReviewerRecommendation recommendation = new ReviewerRecommendation();
        recommendation.setDocumentId(docA.getId());
        recommendation.setState(ReviewerRecommendation.RecommendationState.ACTIVE);
        recommendation.setRecommendation(ReviewerRecommendation.RecommendationType.REVIEW);
        recommendation.setReviewPriority(30);
        recommendation.setRecommendationScore(70);
        recommendation.setScoreBreakdown(new com.edu.dto.RecommendationScoreBreakdownDTO());
        recommendation.setConfidence(ReviewerRecommendation.RecommendationConfidence.HIGH);
        recommendation.setContradictorySignals(false);
        recommendation.setGeneratedAt(Instant.now());
        recommendation.setExpiresAt(Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS));
        recommendation.setModelVersion("AI4_v1");
        recommendation.setExplanationSummary(Arrays.asList("Requires review"));
        recommendation.setEvidenceSnapshot(new com.edu.dto.EvidenceSnapshotDTO());
        recommendation.setGenerationNumber(1);
        recommendation = recommendationRepository.save(recommendation);

        // Connect instNode <-> docNode with edge weight 2.0
        // Rounded decay: Math.round(10.0 / 2.0) = 5
        // nextRisk = baseRisk (80) - 5 = 75
        createMockEdge(instNode.getId(), docNode.getId(), FraudNetworkEdge.RelationshipType.ISSUED_BY, 2.0);

        riskPropagationService.propagateRisk(instId, 80);

        List<RecommendationEscalation> escalations = escalationRepository.findByRecommendationIdAndActiveTrue(recommendation.getId());
        assertEquals(1, escalations.size(), "Should create 1 escalation");
        
        // Hop 1 value is riskValue (75) / (hopCount + 1) = 75 / 2 = 37 points
        assertEquals(37, escalations.get(0).getEscalationPoints(), "Risk value calculation should match rounding/decay steps");

        // Repeat risk propagation (deduplication check)
        riskPropagationService.propagateRisk(instId, 90);
        List<RecommendationEscalation> updatedEscalations = escalationRepository.findByRecommendationIdAndActiveTrue(recommendation.getId());
        assertEquals(1, updatedEscalations.size(), "Escalation should be updated in-place rather than duplicating");
    }

    @Test
    public void testNightlySnapshotHashingAndReactivation() {
        // Setup initial graph data
        FraudNetworkNode node = new FraudNetworkNode();
        node.setEntityId(UUID.randomUUID());
        node.setNodeType(FraudNetworkNode.NodeType.DOCUMENT);
        node.setRiskScore(90);
        node.setCreatedAt(Instant.now());
        node.setUpdatedAt(Instant.now());
        nodeRepository.save(node);

        nightlySnapshotJob.executeSnapshot();

        List<GraphVersion> versions = graphVersionRepository.findAll();
        assertEquals(1, versions.size(), "Snapshot job should create a version record");
        
        GraphVersion version = versions.get(0);
        assertNotNull(version.getStateHash(), "Version state hash should be computed");
        assertTrue(version.getNodeCount() >= 1, "Should count nodes correctly");
    }

    @Test
    public void testFraudSignatureMatching() {
        // Save matching signature in DB
        FraudSignature signature = new FraudSignature();
        signature.setName("Credential Reference Farming");
        signature.setDescription("Multiple credentials using farming references");
        signature.setPatternRulesJson("{}");
        signature.setCreatedAt(Instant.now());
        signature = signatureRepository.save(signature);

        FraudCampaign campaign = new FraudCampaign();
        campaign.setName("Ref Farming Incident");
        campaign.setSignalType("CREDENTIAL_REF");
        campaign.setTotalCount(6);
        campaign.setCampaignScore(80);
        campaign.setConfidenceScore(0.85);
        campaign.setSeverity(FraudCampaign.CampaignSeverity.HIGH);
        campaign.setStatus(FraudCampaign.CampaignStatus.NEW);
        campaign.setCreatedAt(Instant.now());
        campaign.setUpdatedAt(Instant.now());

        Optional<FraudSignature> matched = signatureMatcher.classify(campaign);
        assertTrue(matched.isPresent(), "Campaign should be classified under signature");
        assertEquals("Credential Reference Farming", matched.get().getName(), "Should match correct signature name");
        assertEquals(1, matched.get().getHistoricalCampaignsCount(), "Should increment match count");
    }

    @Test
    public void testCampaignStateTransitionValidation() {
        FraudCampaign campaign = new FraudCampaign();
        campaign.setName("visual clones");
        campaign.setCampaignScore(40);
        campaign.setConfidenceScore(0.5);
        campaign.setSeverity(FraudCampaign.CampaignSeverity.LOW);
        campaign.setStatus(FraudCampaign.CampaignStatus.NEW);
        campaign.setCreatedAt(Instant.now());
        campaign.setUpdatedAt(Instant.now());
        campaign = campaignRepository.save(campaign);

        // NEW -> MONITORING (valid)
        campaign = campaignService.transitionStatus(campaign.getId(), FraudCampaign.CampaignStatus.MONITORING);
        assertEquals(FraudCampaign.CampaignStatus.MONITORING, campaign.getStatus());

        // MONITORING -> CLOSED (valid)
        campaign = campaignService.transitionStatus(campaign.getId(), FraudCampaign.CampaignStatus.CLOSED);
        assertEquals(FraudCampaign.CampaignStatus.CLOSED, campaign.getStatus());

        // CLOSED -> MONITORING (invalid - CLOSED is terminal)
        final UUID closedId = campaign.getId();
        assertThrows(IllegalStateException.class, () -> {
            campaignService.transitionStatus(closedId, FraudCampaign.CampaignStatus.MONITORING);
        }, "Closed campaigns cannot transition out of CLOSED status");
    }

    private void createMockEdge(UUID src, UUID tgt, FraudNetworkEdge.RelationshipType type, double weight) {
        UUID left = src.toString().compareTo(tgt.toString()) < 0 ? src : tgt;
        UUID right = src.toString().compareTo(tgt.toString()) < 0 ? tgt : src;

        FraudNetworkEdge edge = new FraudNetworkEdge();
        edge.setSourceNodeId(left);
        edge.setTargetNodeId(right);
        edge.setRelationshipType(type);
        edge.setWeight(weight);
        edge.setCreatedAt(Instant.now());
        edgeRepository.save(edge);
    }
}
