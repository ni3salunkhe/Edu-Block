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
    public List<InstitutionTrustProfile> getInstitutions() {
        return trustProfileRepository.findAll();
    }

    @GetMapping("/institutions/{id}")
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
