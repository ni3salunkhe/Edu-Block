package com.edu.controller;

import com.edu.entity.*;
import com.edu.repositories.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.edu.services.AuditService;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/intelligence")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIntelligenceController {

    private final DocumentRepository documentRepository;
    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final FraudCampaignRepository campaignRepository;
    private final FraudClusterRepository clusterRepository;
    private final FraudNetworkNodeRepository nodeRepository;
    private final FraudNetworkEdgeRepository edgeRepository;
    private final IntelligenceEvidenceRepository evidenceRepository;
    private final FraudSignatureRepository signatureRepository;
    private final VerificationRuleConfigRepository ruleConfigRepository;
    private final ReviewerFeedbackRepository feedbackRepository;
    private final AuditService auditService;

    private final GraphVersionRepository graphVersionRepository;
    private final RecommendationEscalationRepository escalationRepository;

    public AdminIntelligenceController(
            DocumentRepository documentRepository,
            InstitutionTrustProfileRepository trustProfileRepository,
            FraudCampaignRepository campaignRepository,
            FraudClusterRepository clusterRepository,
            FraudNetworkNodeRepository nodeRepository,
            FraudNetworkEdgeRepository edgeRepository,
            IntelligenceEvidenceRepository evidenceRepository,
            FraudSignatureRepository signatureRepository,
            VerificationRuleConfigRepository ruleConfigRepository,
            ReviewerFeedbackRepository feedbackRepository,
            AuditService auditService,
            GraphVersionRepository graphVersionRepository,
            RecommendationEscalationRepository escalationRepository) {
        this.documentRepository = documentRepository;
        this.trustProfileRepository = trustProfileRepository;
        this.campaignRepository = campaignRepository;
        this.clusterRepository = clusterRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.evidenceRepository = evidenceRepository;
        this.signatureRepository = signatureRepository;
        this.ruleConfigRepository = ruleConfigRepository;
        this.feedbackRepository = feedbackRepository;
        this.auditService = auditService;
        this.graphVersionRepository = graphVersionRepository;
        this.escalationRepository = escalationRepository;
    }

    @GetMapping("/global-overview")
    public ResponseEntity<Map<String, Object>> getGlobalOverview() {
        Map<String, Object> response = new HashMap<>();
        
        long docsProcessed = documentRepository.count();
        long aiVerified = documentRepository.findAll().stream().filter(d -> "AI_VERIFIED".equals(d.getStatus().name())).count();
        long aiFlagged = documentRepository.findAll().stream().filter(d -> "AI_FLAGGED".equals(d.getStatus().name())).count();
        long institutions = trustProfileRepository.count();
        long activeCampaigns = campaignRepository.count(); // could filter by status if we add status to FraudCampaign
        long activeClusters = clusterRepository.count();
        
        List<InstitutionTrustProfile> profiles = trustProfileRepository.findAll();
        double avgTrustScore = profiles.stream().mapToInt(InstitutionTrustProfile::getTrustScore).average().orElse(0.0);
        
        long overrides = feedbackRepository.findAll().stream().filter(ReviewerFeedback::getIsOverride).count();

        // Platform Intelligence Score (0-100)
        // Calculated based on health metrics
        double score = 100.0;
        if (docsProcessed > 0) {
            score -= (activeCampaigns * 2);
            score -= (activeClusters * 0.5);
            score += (avgTrustScore - 50) * 0.1;
        }
        score = Math.max(0, Math.min(100, score));

        response.put("platformIntelligenceScore", (int) score);
        response.put("documentsProcessed", docsProcessed);
        response.put("aiVerified", aiVerified);
        response.put("aiFlagged", aiFlagged);
        response.put("institutions", institutions);
        response.put("activeCampaigns", activeCampaigns);
        response.put("activeClusters", activeClusters);
        response.put("averageTrustScore", avgTrustScore);
        response.put("reviewerOverrides", overrides);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaigns")
    public ResponseEntity<Page<FraudCampaign>> getAllCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(campaignRepository.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<FraudCampaign> getCampaignById(@PathVariable UUID id) {
        return campaignRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clusters")
    public ResponseEntity<Page<FraudCluster>> getAllClusters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clusterRepository.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/clusters/{id}")
    public ResponseEntity<FraudCluster> getClusterById(@PathVariable UUID id) {
        return clusterRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/network-summary")
    public ResponseEntity<Map<String, Object>> getNetworkSummary() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalNodes", nodeRepository.count());
        response.put("totalEdges", edgeRepository.count());
        List<FraudCluster> clusters = clusterRepository.findAll();
        response.put("totalClusters", clusters.size());
        
        double avgSize = clusters.isEmpty() ? 0 : clusters.stream().mapToInt(FraudCluster::getNodeCount).average().orElse(0);
        int maxSize = clusters.isEmpty() ? 0 : clusters.stream().mapToInt(FraudCluster::getNodeCount).max().orElse(0);
        
        response.put("averageClusterSize", avgSize);
        response.put("largestClusterSize", maxSize);
        
        Optional<GraphVersion> latestGraphOpt = graphVersionRepository.findAll().stream().max(Comparator.comparing(GraphVersion::getVersionNumber));
        response.put("graphVersion", latestGraphOpt.map(GraphVersion::getVersionNumber).orElse(0L));
        
        List<RecommendationEscalation> allEscalations = escalationRepository.findAll();
        long activeEscalations = allEscalations.stream().filter(RecommendationEscalation::getActive).count();
        
        Map<String, Long> escalationsByType = allEscalations.stream()
            .collect(Collectors.groupingBy(RecommendationEscalation::getSourceType, Collectors.counting()));
            
        response.put("totalEscalations", allEscalations.size());
        response.put("activeEscalations", activeEscalations);
        response.put("escalationsByType", escalationsByType);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/evidence")
    public ResponseEntity<Page<IntelligenceEvidence>> getEvidence(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(evidenceRepository.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/evidence/{id}")
    public ResponseEntity<IntelligenceEvidence> getEvidenceById(@PathVariable UUID id) {
        return evidenceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/signatures")
    public ResponseEntity<Page<FraudSignature>> getSignatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(signatureRepository.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/rules")
    public ResponseEntity<List<VerificationRuleConfig>> getRules() {
        return ResponseEntity.ok(ruleConfigRepository.findAll());
    }

    @PatchMapping("/rules/{ruleName}")
    public ResponseEntity<?> updateRule(@PathVariable String ruleName, @RequestBody Map<String, Object> updates) {
        Optional<VerificationRuleConfig> ruleOpt = ruleConfigRepository.findById(ruleName);
        if (ruleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String reason = (String) updates.get("reason");
        if (reason == null || reason.trim().length() < 15 || reason.trim().length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid reason between 15 and 500 characters is required."));
        }
        
        VerificationRuleConfig rule = ruleOpt.get();
        if (updates.containsKey("enabled")) {
            rule.setEnabled((Boolean) updates.get("enabled"));
        }
        if (updates.containsKey("modifier")) {
            int modifier = (Integer) updates.get("modifier");
            if (modifier < -100 || modifier > 100) {
                return ResponseEntity.badRequest().body(Map.of("error", "Modifier must be between -100 and 100."));
            }
            rule.setScoreModifier(modifier);
        }
        
        VerificationRuleConfig saved = ruleConfigRepository.save(rule);
        
        // Use a placeholder UUID for admin since we don't have security context handy in this snippet
        UUID adminId = UUID.randomUUID(); 
        auditService.logAdminAction(adminId, "UPDATE_RULE", "RULE_CONFIG", UUID.randomUUID(), 
            "Rule " + ruleName + " updated. Reason: " + reason, "0.0.0.0");
        
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/escalations")
    public ResponseEntity<List<Map<String, Object>>> getEscalations(@RequestParam(required = false) Boolean active) {
        List<RecommendationEscalation> escalations;
        if (active != null) {
            escalations = escalationRepository.findAll().stream()
                .filter(e -> e.getActive().equals(active))
                .sorted(Comparator.comparing(RecommendationEscalation::getCreatedAt).reversed())
                .collect(Collectors.toList());
        } else {
            escalations = escalationRepository.findAll().stream()
                .sorted(Comparator.comparing(RecommendationEscalation::getCreatedAt).reversed())
                .collect(Collectors.toList());
        }
        
        // We will return minimal info, and let the frontend fetch document details if needed
        List<Map<String, Object>> result = new ArrayList<>();
        for (RecommendationEscalation esc : escalations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", esc.getId());
            map.put("recommendationId", esc.getRecommendationId());
            map.put("sourceEntityId", esc.getSourceEntityId());
            map.put("sourceType", esc.getSourceType());
            map.put("escalationPoints", esc.getEscalationPoints());
            map.put("reason", esc.getReason());
            map.put("active", esc.getActive());
            map.put("createdAt", esc.getCreatedAt());
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    @Autowired
    private ReviewerRecommendationRepository reviewerRecommendationRepository;

    @Autowired
    private com.edu.services.DocumentService documentService;

    @PatchMapping("/escalations/{id}/resolve")
    public ResponseEntity<?> resolveEscalation(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Optional<RecommendationEscalation> escOpt = escalationRepository.findById(id);
        if (escOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        RecommendationEscalation esc = escOpt.get();
        esc.setActive(false);
        escalationRepository.save(esc);
        
        UUID adminId = UUID.randomUUID(); 
        auditService.logAdminAction(adminId, "RESOLVE_ESCALATION", "ESCALATION", esc.getId(), 
            "Escalation resolved. Note: " + body.getOrDefault("note", "None"), "0.0.0.0");
            
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }

    @PatchMapping("/escalations/{id}/reject")
    public ResponseEntity<?> rejectDocumentFromEscalation(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Optional<RecommendationEscalation> escOpt = escalationRepository.findById(id);
        if (escOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        RecommendationEscalation esc = escOpt.get();
        esc.setActive(false);
        escalationRepository.save(esc);

        // Fetch Recommendation to get Document ID
        Optional<ReviewerRecommendation> recOpt = reviewerRecommendationRepository.findById(esc.getRecommendationId());
        if (recOpt.isPresent()) {
            UUID documentId = recOpt.get().getDocumentId();
            String comment = body.getOrDefault("note", "Rejected by Admin via Escalation Console.");
            documentService.rejectWithComment(documentId, comment);
        }

        UUID adminId = UUID.randomUUID(); 
        auditService.logAdminAction(adminId, "REJECT_ESCALATION", "ESCALATION", esc.getId(), 
            "Admin rejected document from escalation. Note: " + body.getOrDefault("note", "None"), "0.0.0.0");
            
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }
}
