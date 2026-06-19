package com.edu.controller;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.services.InstitutionUserService;
import com.edu.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;

@RestController
@RequestMapping("/api/institution/intelligence")
@PreAuthorize("hasRole('INSTITUTION')")
public class InstitutionIntelligenceController {

    private final InstitutionUserService institutionUserService;
    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final InstitutionTrustHistoryRepository trustHistoryRepository;
    private final InstitutionRiskEventRepository riskEventRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVerificationResultRepository dvrRepository;

    public InstitutionIntelligenceController(
            InstitutionUserService institutionUserService,
            InstitutionTrustProfileRepository trustProfileRepository,
            InstitutionTrustHistoryRepository trustHistoryRepository,
            InstitutionRiskEventRepository riskEventRepository,
            DocumentRepository documentRepository,
            DocumentVerificationResultRepository dvrRepository) {
        this.institutionUserService = institutionUserService;
        this.trustProfileRepository = trustProfileRepository;
        this.trustHistoryRepository = trustHistoryRepository;
        this.riskEventRepository = riskEventRepository;
        this.documentRepository = documentRepository;
        this.dvrRepository = dvrRepository;
    }

    private UUID getInstitutionId() {
        UUID userId = SecurityUtil.getCurrentUserId();
        Optional<Institution> instOpt = institutionUserService.getInstitutionForUser(userId);
        return instOpt.map(Institution::getId).orElse(null);
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();

        InstitutionTrustProfile profile = trustProfileRepository.findById(instId).orElse(null);
        if (profile == null) return ResponseEntity.notFound().build();

        List<InstitutionTrustHistory> history = trustHistoryRepository.findByInstitutionIdOrderByTimestampDesc(instId);
        int trend = 0;
        if (history.size() >= 2) {
            trend = profile.getTrustScore() - history.get(1).getPreviousScore();
        } else if (history.size() == 1) {
            trend = profile.getTrustScore() - history.get(0).getPreviousScore();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("trustScore", profile.getTrustScore());
        res.put("trustTier", profile.getTrustTier());
        res.put("trend", trend);
        
        long activeRisks = riskEventRepository.findByInstitutionIdAndResolvedFalse(instId).size();
        res.put("riskLevel", activeRisks > 2 ? "HIGH" : activeRisks > 0 ? "MEDIUM" : "LOW");
        
        Map<String, Object> components = new HashMap<>();
        if (profile.getScoreBreakdown() != null) {
            components.put("Accreditation Contribution", profile.getScoreBreakdown().getAccreditationContribution());
            components.put("Verification Contribution", profile.getScoreBreakdown().getVerificationContribution());
            components.put("Fraud Penalty", profile.getScoreBreakdown().getFraudPenalty());
            components.put("Risk Penalty", profile.getScoreBreakdown().getRiskPenalty());
        }
        res.put("scoreBreakdown", components);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/trust-history")
    public ResponseEntity<List<InstitutionTrustHistory>> getTrustHistory() {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(trustHistoryRepository.findByInstitutionIdOrderByTimestampDesc(instId));
    }

    @GetMapping("/risk-events")
    public ResponseEntity<List<InstitutionRiskEvent>> getRiskEvents() {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(riskEventRepository.findByInstitutionIdOrderByTimestampDesc(instId));
    }

    @GetMapping("/quality")
    public ResponseEntity<Map<String, Object>> getCredentialQuality() {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();

        List<Document> docs = documentRepository.findByIssuingInstitutionId(instId);
        long totalIssued = docs.size();
        
        long verified = docs.stream().filter(d -> d.getStatus() == Document.Status.APPROVED).count();
        long aiVerified = docs.stream().filter(d -> d.getStatus() == Document.Status.AI_VERIFIED).count();
        long aiFlagged = docs.stream().filter(d -> d.getStatus() == Document.Status.AI_FLAGGED).count();
        long pending = docs.stream().filter(d -> d.getStatus() == Document.Status.UPLOADED || d.getStatus() == Document.Status.AI_REVIEW_REQUIRED).count();
        long anchored = docs.stream().filter(d -> d.getBlockchainTx() != null).count();

        Map<String, Object> res = new HashMap<>();
        res.put("totalIssued", totalIssued);
        res.put("verified", verified);
        res.put("aiVerified", aiVerified);
        res.put("aiFlagged", aiFlagged);
        res.put("pending", pending);
        res.put("anchored", anchored);

        // Calculate confidence distribution
        List<DocumentVerificationResult> dvrs = new ArrayList<>();
        for (Document d : docs) {
            dvrRepository.findById(d.getId()).ifPresent(dvrs::add);
        }

        long veryHigh = dvrs.stream().filter(r -> r.getConfidenceScore() >= 90).count();
        long high = dvrs.stream().filter(r -> r.getConfidenceScore() >= 80 && r.getConfidenceScore() < 90).count();
        long medium = dvrs.stream().filter(r -> r.getConfidenceScore() >= 60 && r.getConfidenceScore() < 80).count();
        long low = dvrs.stream().filter(r -> r.getConfidenceScore() < 60).count();

        res.put("confidenceDistribution", Map.of(
            "VERY_HIGH", veryHigh,
            "HIGH", high,
            "MEDIUM", medium,
            "LOW", low
        ));

        // Group by Status
        Map<String, Long> statusBreakdown = docs.stream().collect(Collectors.groupingBy(d -> d.getStatus().name(), Collectors.counting()));
        res.put("statusBreakdown", statusBreakdown);

        // Group by Type
        Map<String, Long> typeBreakdown = docs.stream().collect(Collectors.groupingBy(
            d -> d.getDocumentType() != null ? d.getDocumentType() : "UNKNOWN", 
            Collectors.counting()
        ));
        res.put("credentialTypes", typeBreakdown);

        // Issuance Trends (Simple mock or actual calculation based on createdAt)
        Instant now = Instant.now();
        long daily = docs.stream().filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(now.minusSeconds(86400))).count();
        long weekly = docs.stream().filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(now.minusSeconds(86400 * 7))).count();
        long monthly = docs.stream().filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(now.minusSeconds(86400 * 30))).count();
        res.put("issuanceTrends", Map.of("daily", daily, "weekly", weekly, "monthly", monthly));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> getBenchmark() {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();

        InstitutionTrustProfile myProfile = trustProfileRepository.findById(instId).orElse(null);
        if (myProfile == null) return ResponseEntity.notFound().build();

        List<InstitutionTrustProfile> allProfiles = trustProfileRepository.findAll();
        
        double avgScore = allProfiles.stream().mapToInt(InstitutionTrustProfile::getTrustScore).average().orElse(0);
        int topQuartileScore = 0;
        if (!allProfiles.isEmpty()) {
            List<Integer> scores = allProfiles.stream().map(InstitutionTrustProfile::getTrustScore).sorted().collect(Collectors.toList());
            topQuartileScore = scores.get((int) (scores.size() * 0.75));
        }

        int rank = 0;
        for (InstitutionTrustProfile p : allProfiles) {
            if (p.getTrustScore() <= myProfile.getTrustScore()) rank++;
        }
        double percentile = allProfiles.isEmpty() ? 100.0 : ((double) rank / allProfiles.size()) * 100.0;

        Map<String, Object> res = new HashMap<>();
        res.put("yourScore", myProfile.getTrustScore());
        res.put("industryAverage", avgScore);
        res.put("topQuartile", topQuartileScore);
        res.put("percentileRank", percentile);
        
        // Averages for other metrics
        double avgVerification = allProfiles.stream().mapToDouble(InstitutionTrustProfile::getVerificationSuccessRate).average().orElse(0);
        double avgFraud = allProfiles.stream().mapToDouble(InstitutionTrustProfile::getFraudRate).average().orElse(0);
        
        res.put("yourVerificationSuccess", myProfile.getVerificationSuccessRate());
        res.put("industryVerificationSuccess", avgVerification);
        res.put("yourFraudRate", myProfile.getFraudRate());
        res.put("industryFraudRate", avgFraud);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/reputation")
    public ResponseEntity<Map<String, Object>> getReputation() {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();

        InstitutionTrustProfile myProfile = trustProfileRepository.findById(instId).orElse(null);
        if (myProfile == null) return ResponseEntity.notFound().build();

        Map<String, Object> res = new HashMap<>();
        res.put("trustGrowth", myProfile.getTrustScore() > 80 ? "Positive" : "Stable");
        res.put("verificationQuality", myProfile.getVerificationSuccessRate() > 90 ? "Excellent" : "Needs Improvement");
        res.put("credentialReliability", "High");
        res.put("riskExposure", myProfile.getFraudRate() > 5 ? "Elevated" : "Low");

        List<String> summaries = new ArrayList<>();
        if (myProfile.getVerificationSuccessRate() > 85) summaries.add("Trust improving due to high verification success.");
        if (myProfile.getFraudRate() < 2) summaries.add("Fraud exposure reduced, contributing to positive reputation.");
        if (myProfile.getTrustScore() > 90) summaries.add("Maintaining Top Tier trust status.");
        
        if (summaries.isEmpty()) summaries.add("Reputation is currently stable. Focus on credential verification rates.");
        
        res.put("summaries", summaries);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations() {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();

        InstitutionTrustProfile myProfile = trustProfileRepository.findById(instId).orElse(null);
        if (myProfile == null) return ResponseEntity.notFound().build();

        List<Map<String, Object>> recommendations = new ArrayList<>();

        if (myProfile.getVerificationSuccessRate() < 90) {
            recommendations.add(Map.of(
                "category", "Quality",
                "priority", "HIGH",
                "description", "Increase credential metadata completeness to improve automated verification success.",
                "expectedImpact", "+5 Trust Points"
            ));
        }

        if (myProfile.getFraudRate() > 3) {
            recommendations.add(Map.of(
                "category", "Compliance",
                "priority", "CRITICAL",
                "description", "Investigate recent spike in flagged credentials. Review issuance controls.",
                "expectedImpact", "Risk Mitigation"
            ));
        }

        List<Document> docs = documentRepository.findByIssuingInstitutionId(instId);
        long unanchored = docs.stream().filter(d -> d.getBlockchainTx() == null).count();
        if (unanchored > 0) {
            recommendations.add(Map.of(
                "category", "Blockchain",
                "priority", "MEDIUM",
                "description", "Ensure pending credentials are fully anchored to the blockchain for immutability.",
                "expectedImpact", "Improved Reliability"
            ));
        }

        if (recommendations.isEmpty()) {
            recommendations.add(Map.of(
                "category", "Trust",
                "priority", "LOW",
                "description", "Continue current issuance patterns. No critical adjustments needed.",
                "expectedImpact", "Maintain Status"
            ));
        }

        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/simulate")
    public ResponseEntity<Map<String, Object>> getSimulation(
            @RequestParam(defaultValue = "0") double fraudDelta,
            @RequestParam(defaultValue = "0") double verificationDelta) {
        UUID instId = getInstitutionId();
        if (instId == null) return ResponseEntity.notFound().build();

        InstitutionTrustProfile myProfile = trustProfileRepository.findById(instId).orElse(null);
        if (myProfile == null) return ResponseEntity.notFound().build();

        int currentScore = myProfile.getTrustScore();
        
        // Simplified deterministic engine: 
        // +0.5 points per +1% Verification Success
        // -2.0 points per +1% Fraud Rate
        double verificationImpact = verificationDelta * 0.5;
        double fraudImpact = fraudDelta * -2.0;
        
        int scoreDelta = (int) Math.round(verificationImpact + fraudImpact);
        int projectedScore = Math.min(100, Math.max(0, currentScore + scoreDelta));

        Map<String, Object> res = new HashMap<>();
        res.put("currentScore", currentScore);
        res.put("projectedScore", projectedScore);
        res.put("scoreDelta", scoreDelta);
        res.put("verificationImpact", (int) Math.round(verificationImpact));
        res.put("fraudImpact", (int) Math.round(fraudImpact));

        return ResponseEntity.ok(res);
    }
}
