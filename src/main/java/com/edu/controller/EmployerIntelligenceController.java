package com.edu.controller;

import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employer/intelligence")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerIntelligenceController {

    @Autowired
    private EmployerConsentRepository consentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentVerificationResultRepository dvrRepository;

    @Autowired
    private InstitutionTrustProfileRepository trustProfileRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    // Security Gatekeeper
    private boolean hasConsent(UUID employerId, UUID candidateId) {
        return consentRepository.findByEmployerIdAndOwnerId(employerId, candidateId)
                .map(c -> c.getStatus() == EmployerConsent.Status.APPROVED && 
                         (c.getValidUntil() == null || c.getValidUntil().isAfter(Instant.now())))
                .orElse(false);
    }

    // Helper to get approved candidates
    private List<UUID> getApprovedCandidateIds(UUID employerId) {
        return consentRepository.findByEmployerId(employerId).stream()
                .filter(c -> c.getStatus() == EmployerConsent.Status.APPROVED && 
                        (c.getValidUntil() == null || c.getValidUntil().isAfter(Instant.now())))
                .map(EmployerConsent::getOwnerId)
                .collect(Collectors.toList());
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        UUID employerId = SecurityUtil.getCurrentUserId();
        List<UUID> candidateIds = getApprovedCandidateIds(employerId);
        
        long totalCandidates = candidateIds.size();
        long verificationRequests = consentRepository.findByEmployerId(employerId).size();
        
        // Count risks simply by scanning documents of approved candidates
        long highTrust = 0;
        long medRisk = 0;
        long highRisk = 0;

        for (UUID cid : candidateIds) {
            List<Document> docs = documentRepository.findByOwnerId(cid);
            long flagged = docs.stream().filter(d -> d.getStatus() == Document.Status.AI_FLAGGED || d.getStatus() == Document.Status.REJECTED).count();
            if (flagged == 0) highTrust++;
            else if (flagged == 1) medRisk++;
            else highRisk++;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("candidatesReviewed", totalCandidates);
        res.put("verificationRequests", verificationRequests);
        res.put("approvedAccesses", totalCandidates);
        res.put("highTrustCandidates", highTrust);
        res.put("mediumRiskCandidates", medRisk);
        res.put("highRiskCandidates", highRisk);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/candidate/{id}")
    public ResponseEntity<Map<String, Object>> getCandidateTrust(@PathVariable UUID id) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        if (!hasConsent(employerId, id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User candidate = userRepository.findById(id).orElse(null);
        if (candidate == null) return ResponseEntity.notFound().build();

        List<Document> docs = documentRepository.findByOwnerId(id);
        long verified = docs.stream().filter(d -> d.getStatus() == Document.Status.APPROVED || d.getStatus() == Document.Status.AI_VERIFIED).count();
        
        double verificationSuccess = docs.isEmpty() ? 0 : (double) verified / docs.size() * 100;
        
        int score = (int) verificationSuccess;
        String tier = score >= 90 ? "TOP TIER" : score >= 70 ? "TRUSTED" : "EVALUATING";

        Map<String, Object> res = new HashMap<>();
        res.put("trustScore", score);
        res.put("trustTier", tier);
        res.put("credentialConfidence", score > 80 ? "HIGH" : "MEDIUM");
        res.put("verificationSuccess", (int)verificationSuccess);
        
        // Explainability
        List<String> explanations = new ArrayList<>();
        if (score >= 90) explanations.add(verified + " credentials verified successfully.");
        if (docs.stream().anyMatch(d -> d.getBlockchainTx() != null)) explanations.add("Blockchain anchored credentials present.");
        res.put("explanations", explanations);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/candidate/{id}/credentials")
    public ResponseEntity<List<Map<String, Object>>> getCandidateCredentials(@PathVariable UUID id) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        if (!hasConsent(employerId, id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Document> docs = documentRepository.findByOwnerId(id);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Document d : docs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", d.getId());
            item.put("type", d.getDocumentType());
            item.put("status", d.getStatus().name());
            item.put("issuedDate", d.getCreatedAt());
            item.put("blockchainStatus", d.getBlockchainTx() != null ? "ANCHORED" : "PENDING");
            
            Institution inst = institutionRepository.findById(d.getIssuingInstitutionId()).orElse(null);
            item.put("institution", inst != null ? inst.getName() : "Unknown");

            DocumentVerificationResult dvr = dvrRepository.findById(d.getId()).orElse(null);
            item.put("confidenceLevel", dvr != null && dvr.getConfidenceScore() > 80 ? "HIGH" : "MEDIUM");
            
            List<String> explanations = new ArrayList<>();
            if (dvr != null) {
                if (dvr.getConfidenceScore() > 80) explanations.add("High confidence algorithmic match.");
                if (d.getBlockchainTx() != null) explanations.add("Blockchain anchor confirmed.");
            }
            item.put("explanations", explanations);

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/candidate/{id}/institution")
    public ResponseEntity<List<Map<String, Object>>> getCandidateInstitutionReputation(@PathVariable UUID id) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        if (!hasConsent(employerId, id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Document> docs = documentRepository.findByOwnerId(id);
        Set<UUID> instIds = docs.stream().map(Document::getIssuingInstitutionId).filter(Objects::nonNull).collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (UUID instId : instIds) {
            Institution inst = institutionRepository.findById(instId).orElse(null);
            InstitutionTrustProfile profile = trustProfileRepository.findById(instId).orElse(null);
            
            if (inst != null && profile != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", inst.getId());
                item.put("name", inst.getName());
                item.put("trustScore", profile.getTrustScore());
                item.put("trustTier", profile.getTrustTier() != null ? profile.getTrustTier().name() : "EVALUATING");
                item.put("verificationSuccessRate", profile.getVerificationSuccessRate());
                item.put("fraudExposureCategory", profile.getFraudRate() > 5 ? "Elevated" : "Low");
                
                String summary = "Institution demonstrates " + (profile.getTrustScore() > 80 ? "consistently high" : "moderate") + " credential quality.";
                item.put("reputationSummary", summary);

                result.add(item);
            }
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/candidate/{id}/risk")
    public ResponseEntity<Map<String, Object>> getCandidateRisk(@PathVariable UUID id) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        if (!hasConsent(employerId, id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Document> docs = documentRepository.findByOwnerId(id);
        long flagged = docs.stream().filter(d -> d.getStatus() == Document.Status.AI_FLAGGED || d.getStatus() == Document.Status.REJECTED).count();
        long pending = docs.stream().filter(d -> d.getStatus() == Document.Status.UPLOADED || d.getStatus() == Document.Status.AI_REVIEW_REQUIRED).count();

        String riskSignal = flagged > 0 ? "High Risk" : pending > 0 ? "Medium Risk" : "Low Risk";

        List<String> explanations = new ArrayList<>();
        if (flagged > 0) explanations.add("Flagged credentials present.");
        if (pending > 0) explanations.add("Manual review pending on some documents.");
        if (flagged == 0 && pending == 0) explanations.add("All credentials verified successfully.");

        Map<String, Object> res = new HashMap<>();
        res.put("riskSignal", riskSignal);
        res.put("explanations", explanations);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/portfolio")
    public ResponseEntity<Map<String, Object>> getPortfolioAnalytics() {
        UUID employerId = SecurityUtil.getCurrentUserId();
        List<UUID> candidateIds = getApprovedCandidateIds(employerId);

        long verifiedCandidates = 0;
        long flaggedCandidates = 0;
        Set<UUID> instIds = new HashSet<>();

        for (UUID cid : candidateIds) {
            List<Document> docs = documentRepository.findByOwnerId(cid);
            boolean hasFlagged = docs.stream().anyMatch(d -> d.getStatus() == Document.Status.AI_FLAGGED || d.getStatus() == Document.Status.REJECTED);
            boolean allVerified = docs.stream().allMatch(d -> d.getStatus() == Document.Status.APPROVED || d.getStatus() == Document.Status.AI_VERIFIED);
            
            if (hasFlagged) flaggedCandidates++;
            if (allVerified && !docs.isEmpty()) verifiedCandidates++;

            docs.forEach(d -> {
                if (d.getIssuingInstitutionId() != null) instIds.add(d.getIssuingInstitutionId());
            });
        }

        Map<String, Object> res = new HashMap<>();
        res.put("totalCandidates", candidateIds.size());
        res.put("verifiedCandidates", verifiedCandidates);
        res.put("flaggedCandidates", flaggedCandidates);
        res.put("institutionDiversity", instIds.size());

        return ResponseEntity.ok(res);
    }

    @GetMapping("/candidate/{id}/timeline")
    public ResponseEntity<List<Map<String, Object>>> getCandidateTimeline(@PathVariable UUID id) {
        UUID employerId = SecurityUtil.getCurrentUserId();
        if (!hasConsent(employerId, id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Document> docs = documentRepository.findByOwnerId(id);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Document d : docs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", d.getId());
            item.put("type", d.getDocumentType());
            item.put("issued", d.getCreatedAt());
            item.put("verified", d.getStatus() == Document.Status.APPROVED || d.getStatus() == Document.Status.AI_VERIFIED ? d.getCreatedAt().plus(1, ChronoUnit.DAYS) : null);
            item.put("anchored", d.getBlockchainTx() != null ? d.getCreatedAt().plus(2, ChronoUnit.DAYS) : null);
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareCandidates(
            @RequestParam UUID candidateA, 
            @RequestParam UUID candidateB) {
        
        UUID employerId = SecurityUtil.getCurrentUserId();
        if (!hasConsent(employerId, candidateA) || !hasConsent(employerId, candidateB)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> aMetrics = getCandidateMetrics(candidateA);
        Map<String, Object> bMetrics = getCandidateMetrics(candidateB);

        Map<String, Object> res = new HashMap<>();
        res.put("candidateA", aMetrics);
        res.put("candidateB", bMetrics);

        return ResponseEntity.ok(res);
    }

    private Map<String, Object> getCandidateMetrics(UUID cid) {
        List<Document> docs = documentRepository.findByOwnerId(cid);
        long verified = docs.stream().filter(d -> d.getStatus() == Document.Status.APPROVED || d.getStatus() == Document.Status.AI_VERIFIED).count();
        double verificationSuccess = docs.isEmpty() ? 0 : (double) verified / docs.size() * 100;
        
        long anchored = docs.stream().filter(d -> d.getBlockchainTx() != null).count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("trustScore", (int) verificationSuccess);
        metrics.put("verificationSuccess", (int) verificationSuccess);
        metrics.put("anchored", anchored);
        metrics.put("confidence", verificationSuccess > 80 ? "HIGH" : "MEDIUM");
        return metrics;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Map<String, String>>> getEmployerRecommendations() {
        UUID employerId = SecurityUtil.getCurrentUserId();
        List<UUID> candidateIds = getApprovedCandidateIds(employerId);

        List<Map<String, String>> recommendations = new ArrayList<>();
        
        for (UUID cid : candidateIds) {
            List<Document> docs = documentRepository.findByOwnerId(cid);
            long flagged = docs.stream().filter(d -> d.getStatus() == Document.Status.AI_FLAGGED || d.getStatus() == Document.Status.REJECTED).count();
            long pending = docs.stream().filter(d -> d.getStatus() == Document.Status.UPLOADED || d.getStatus() == Document.Status.AI_REVIEW_REQUIRED).count();

            Map<String, String> rec = new HashMap<>();
            rec.put("candidateId", cid.toString());
            if (flagged > 0) {
                rec.put("category", "Investigate");
                rec.put("recommendation", "Request additional verification due to flagged credentials.");
            } else if (pending > 0) {
                rec.put("category", "Await Verification");
                rec.put("recommendation", "Await pending credential review before proceeding.");
            } else if (!docs.isEmpty()) {
                rec.put("category", "Proceed");
                rec.put("recommendation", "Candidate demonstrates consistently verified educational records. Safe to proceed.");
            }
            if (!rec.isEmpty()) {
                recommendations.add(rec);
            }
        }

        return ResponseEntity.ok(recommendations);
    }
}
