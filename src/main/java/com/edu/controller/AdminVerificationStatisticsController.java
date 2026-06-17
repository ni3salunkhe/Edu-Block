package com.edu.controller;

import com.edu.dto.VerificationStatsDTO;
import com.edu.services.VerificationStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/verification")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVerificationStatisticsController {

    private final VerificationStatisticsService statisticsService;

    public AdminVerificationStatisticsController(VerificationStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<VerificationStatsDTO> getVerificationStats() {
        return ResponseEntity.ok(statisticsService.getVerificationStats());
    }
}
