package com.edu.controller;

import com.edu.entity.InstitutionRiskEvent;
import com.edu.entity.InstitutionTrustHistory;
import com.edu.entity.InstitutionTrustProfile;
import com.edu.repositories.InstitutionRiskEventRepository;
import com.edu.repositories.InstitutionTrustHistoryRepository;
import com.edu.repositories.InstitutionTrustProfileRepository;
import com.edu.services.InstitutionTrustService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/trust")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTrustController {

    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final InstitutionTrustHistoryRepository trustHistoryRepository;
    private final InstitutionRiskEventRepository riskEventRepository;
    private final InstitutionTrustService trustService;

    public AdminTrustController(InstitutionTrustProfileRepository trustProfileRepository,
                                InstitutionTrustHistoryRepository trustHistoryRepository,
                                InstitutionRiskEventRepository riskEventRepository,
                                InstitutionTrustService trustService) {
        this.trustProfileRepository = trustProfileRepository;
        this.trustHistoryRepository = trustHistoryRepository;
        this.riskEventRepository = riskEventRepository;
        this.trustService = trustService;
    }

    @GetMapping("/institutions")
    public List<Map<String, Object>> getInstitutions() {
        return trustProfileRepository.findAllWithInstitution().stream().map(profile -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", profile.getInstitutionId());
            map.put("trustScore", profile.getTrustScore());
            map.put("trustTier", profile.getTrustTier());
            map.put("verificationSuccessRate", profile.getVerificationSuccessRate());
            map.put("fraudDetectionRate", profile.getFraudRate());
            
            // Avoid lazy load exception by wrapping in try-catch or just fetching name
            try {
                map.put("institutionName", profile.getInstitution().getName());
            } catch (Exception e) {
                map.put("institutionName", "Unknown Institution");
            }
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/institutions/{id}")
    public ResponseEntity<Map<String, Object>> getInstitutionTrustDetails(@PathVariable("id") UUID id) {
        InstitutionTrustProfile profile = trustService.getOrCreateProfile(id);
        List<InstitutionTrustHistory> history = trustHistoryRepository.findByInstitutionIdOrderByTimestampDesc(id);
        List<InstitutionRiskEvent> riskEvents = riskEventRepository.findByInstitutionIdOrderByTimestampDesc(id);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> profileMap = new HashMap<>();
        profileMap.put("institutionId", profile.getInstitutionId());
        profileMap.put("trustScore", profile.getTrustScore());
        profileMap.put("trustTier", profile.getTrustTier());
        profileMap.put("fraudRate", profile.getFraudRate());
        profileMap.put("verificationSuccessRate", profile.getVerificationSuccessRate());
        profileMap.put("averageConfidenceScore", profile.getAverageConfidenceScore());
        profileMap.put("totalIssuedCredentials", profile.getTotalIssuedCredentials());
        profileMap.put("totalVerifiedCredentials", profile.getTotalVerifiedCredentials());
        profileMap.put("totalFraudSignals", profile.getTotalFraudSignals());
        profileMap.put("scoreBreakdown", profile.getScoreBreakdown());
        profileMap.put("lastCalculatedAt", profile.getLastCalculatedAt());
        
        try {
            profileMap.put("institutionName", profile.getInstitution().getName());
        } catch (Exception e) {
            profileMap.put("institutionName", "Unknown Institution");
        }

        response.put("profile", profileMap);
        response.put("history", history);
        response.put("riskEvents", riskEvents);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getGlobalStatistics() {
        List<InstitutionTrustProfile> profiles = trustProfileRepository.findAll();
        
        long totalInstitutions = profiles.size();
        double averageScore = profiles.stream()
                .mapToInt(InstitutionTrustProfile::getTrustScore)
                .average()
                .orElse(0.0);

        Map<String, Long> tierDistribution = new HashMap<>();
        for (InstitutionTrustProfile.TrustTier tier : InstitutionTrustProfile.TrustTier.values()) {
            tierDistribution.put(tier.name(), 0L);
        }
        for (InstitutionTrustProfile p : profiles) {
            String tierName = p.getTrustTier().name();
            tierDistribution.put(tierName, tierDistribution.getOrDefault(tierName, 0L) + 1);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalInstitutionsMapped", totalInstitutions);
        response.put("averageTrustScore", averageScore);
        response.put("tierDistribution", tierDistribution);

        return ResponseEntity.ok(response);
    }
}
