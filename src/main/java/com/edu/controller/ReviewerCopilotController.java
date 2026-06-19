package com.edu.controller;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.InstitutionTrustService;
import com.edu.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/verifier/queue")
@PreAuthorize("hasRole('VERIFIER')")
public class ReviewerCopilotController {

    private final ReviewerRecommendationRepository recommendationRepository;
    private final RecommendationEscalationRepository escalationRepository;
    private final InstitutionTrustService trustService;
    private final InstitutionTrustHistoryRepository trustHistoryRepository;
    private final InstitutionRiskEventRepository riskEventRepository;
    private final FraudNetworkNodeRepository nodeRepository;
    private final FraudClusterRepository clusterRepository;
    private final FraudClusterMemberRepository memberRepository;
    private final FraudCampaignRepository campaignRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentVerificationResultRepository verificationResultRepository;
    private final InstitutionRepository institutionRepository;

    public ReviewerCopilotController(ReviewerRecommendationRepository recommendationRepository,
                                     RecommendationEscalationRepository escalationRepository,
                                     InstitutionTrustService trustService,
                                     InstitutionTrustHistoryRepository trustHistoryRepository,
                                     InstitutionRiskEventRepository riskEventRepository,
                                     FraudNetworkNodeRepository nodeRepository,
                                     FraudClusterRepository clusterRepository,
                                     FraudClusterMemberRepository memberRepository,
                                     FraudCampaignRepository campaignRepository,
                                     DocumentRepository documentRepository,
                                     UserRepository userRepository,
                                     DocumentVerificationResultRepository verificationResultRepository,
                                     InstitutionRepository institutionRepository) {
        this.recommendationRepository = recommendationRepository;
        this.escalationRepository = escalationRepository;
        this.trustService = trustService;
        this.trustHistoryRepository = trustHistoryRepository;
        this.riskEventRepository = riskEventRepository;
        this.nodeRepository = nodeRepository;
        this.clusterRepository = clusterRepository;
        this.memberRepository = memberRepository;
        this.campaignRepository = campaignRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.institutionRepository = institutionRepository;
    }

    @GetMapping("/priority-list")
    public ResponseEntity<List<Map<String, Object>>> getDynamicPriorityQueue() {
        List<ReviewerRecommendation> activeRecs = recommendationRepository.findAllActiveRecommendations();
        List<Map<String, Object>> queue = new ArrayList<>();

        for (ReviewerRecommendation rec : activeRecs) {
            int basePriority = rec.getReviewPriority();
            
            List<RecommendationEscalation> escalations = escalationRepository.findByRecommendationIdAndActiveTrue(rec.getId());
            int totalEscalations = escalations.stream().mapToInt(RecommendationEscalation::getEscalationPoints).sum();
            
            int finalPriority = Math.min(100, Math.max(0, basePriority + totalEscalations));

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("recommendationId", rec.getId());
            entry.put("documentId", rec.getDocumentId());
            entry.put("basePriority", basePriority);
            entry.put("escalationPoints", totalEscalations);
            entry.put("finalPriority", finalPriority);
            entry.put("recommendation", rec.getRecommendation());
            entry.put("explanations", rec.getExplanationSummary());
            entry.put("confidence", rec.getConfidence() != null ? rec.getConfidence().toString() : "MEDIUM");

            // Join Document details
            Optional<Document> docOpt = documentRepository.findById(rec.getDocumentId());
            if (docOpt.isPresent()) {
                Document doc = docOpt.get();
                String fileName = "document.pdf";
                if (doc.getFilePath() != null) {
                    try {
                        String path = doc.getFilePath();
                        int slashIdx = path.lastIndexOf('/');
                        if (slashIdx >= 0 && slashIdx < path.length() - 1) {
                            fileName = path.substring(slashIdx + 1);
                        } else {
                            fileName = Paths.get(path).getFileName().toString();
                        }
                    } catch (Exception e) {
                        fileName = "document.pdf";
                    }
                }
                entry.put("fileName", fileName);
                entry.put("status", doc.getStatus().toString());
                entry.put("source", doc.getSource() != null ? doc.getSource().toString() : "INDIVIDUAL_UPLOAD");
                
                // Join Student / Owner details
                entry.put("ownerName", userRepository.findById(doc.getOwnerId()).map(User::getUsername).orElse("Unknown Student"));

                // Join Verification Result details (Risk Level)
                Optional<DocumentVerificationResult> dvrOpt = verificationResultRepository.findById(doc.getId());
                if (dvrOpt.isPresent()) {
                    DocumentVerificationResult dvr = dvrOpt.get();
                    entry.put("riskLevel", dvr.getRiskLevel() != null ? dvr.getRiskLevel().toString() : "LOW");
                } else {
                    entry.put("riskLevel", "LOW");
                }

                // Join Institution Trust details
                if (doc.getIssuingInstitutionId() != null) {
                    try {
                        String instName = institutionRepository.findById(doc.getIssuingInstitutionId()).map(Institution::getName).orElse("Unknown Institution");
                        entry.put("institutionName", instName);
                        
                        InstitutionTrustProfile trustProfile = trustService.getOrCreateProfile(doc.getIssuingInstitutionId());
                        entry.put("trustTier", trustProfile.getTrustTier().toString());
                        entry.put("trustScore", trustProfile.getTrustScore());
                    } catch (Exception e) {
                        entry.put("institutionName", "Unknown Institution");
                        entry.put("trustTier", "MEDIUM");
                        entry.put("trustScore", 50);
                    }
                } else {
                    entry.put("institutionName", "N/A (Self Uploaded)");
                    entry.put("trustTier", "MEDIUM");
                    entry.put("trustScore", 50);
                }
            } else {
                entry.put("fileName", "document.pdf");
                entry.put("status", "AI_VERIFIED");
                entry.put("source", "INDIVIDUAL_UPLOAD");
                entry.put("ownerName", "Unknown Student");
                entry.put("riskLevel", "LOW");
                entry.put("institutionName", "N/A");
                entry.put("trustTier", "MEDIUM");
                entry.put("trustScore", 50);
            }

            queue.add(entry);
        }

        queue.sort((a, b) -> Integer.compare((Integer) b.get("finalPriority"), (Integer) a.get("finalPriority")));
        return ResponseEntity.ok(queue);
    }

    @GetMapping("/trust/institution/{id}")
    public ResponseEntity<Map<String, Object>> getInstitutionTrustDetails(@PathVariable("id") UUID id) {
        InstitutionTrustProfile profile = trustService.getOrCreateProfile(id);
        List<InstitutionTrustHistory> history = trustHistoryRepository.findByInstitutionIdOrderByTimestampDesc(id);
        List<InstitutionRiskEvent> riskEvents = riskEventRepository.findByInstitutionIdOrderByTimestampDesc(id);

        Map<String, Object> response = new HashMap<>();
        response.put("profile", profile);
        response.put("history", history);
        response.put("riskEvents", riskEvents);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/network/details/{documentId}")
    public ResponseEntity<Map<String, Object>> getNetworkDetails(@PathVariable("documentId") UUID documentId) {
        Map<String, Object> response = new HashMap<>();
        
        // 1. Find Node
        Optional<FraudNetworkNode> nodeOpt = nodeRepository.findByEntityIdAndNodeType(documentId, FraudNetworkNode.NodeType.DOCUMENT);
        if (nodeOpt.isPresent()) {
            FraudNetworkNode node = nodeOpt.get();
            // Find Cluster membership
            List<FraudClusterMember> memberships = memberRepository.findByNodeId(node.getId());
            if (!memberships.isEmpty()) {
                FraudClusterMember membership = memberships.get(0);
                Optional<FraudCluster> clusterOpt = clusterRepository.findById(membership.getClusterId());
                if (clusterOpt.isPresent()) {
                    response.put("cluster", clusterOpt.get());
                    response.put("membership", membership);
                }
            }
        }
        
        // 2. Find Campaigns associated with the document issuing institution or template
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isPresent()) {
            Document doc = docOpt.get();
            UUID instId = doc.getIssuingInstitutionId();
            String pHash = doc.getPerceptualHash();
            
            List<FraudCampaign> activeCampaigns = campaignRepository.findAll().stream()
                .filter(c -> (instId != null && instId.equals(c.getInstitutionId())) ||
                             (pHash != null && pHash.equals(c.getTemplateIdentifier())))
                .collect(Collectors.toList());
                
            if (!activeCampaigns.isEmpty()) {
                response.put("campaign", activeCampaigns.get(0));
            }
        }
        
        // 3. Find escalations
        Optional<ReviewerRecommendation> recOpt = recommendationRepository.findAllActiveRecommendations().stream()
            .filter(r -> r.getDocumentId().equals(documentId))
            .findFirst();
        if (recOpt.isPresent()) {
            List<RecommendationEscalation> escalations = escalationRepository.findByRecommendationIdAndActiveTrue(recOpt.get().getId());
            response.put("escalations", escalations);
        } else {
            response.put("escalations", Collections.emptyList());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/escalate")
    public ResponseEntity<?> manualEscalate(@RequestBody Map<String, String> body) {
        UUID recommendationId = UUID.fromString(body.get("recommendationId"));
        String reason = body.get("reason");
        String notes = body.get("notes");
        
        RecommendationEscalation escalation = new RecommendationEscalation();
        escalation.setRecommendationId(recommendationId);
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        escalation.setSourceEntityId(currentUserId != null ? currentUserId : UUID.randomUUID());
        escalation.setSourceType("MANUAL");
        escalation.setEscalationPoints(50);
        escalation.setReason(reason + (notes != null && !notes.trim().isEmpty() ? " (Notes: " + notes.trim() + ")" : ""));
        escalation.setActive(true);
        escalation.setCreatedAt(Instant.now());
        
        escalationRepository.save(escalation);
        
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Document escalated successfully."));
    }
}
