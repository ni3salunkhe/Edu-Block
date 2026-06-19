package com.edu.controller;

import com.edu.dto.*;
import com.edu.entity.*;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.UserRepository;
import com.edu.services.*;
import com.edu.util.SecurityUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/learning")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLearningController {

    private final CalibrationEngineService calibrationEngineService;
    private final FeedbackIntelligenceService feedbackIntelligenceService;
    private final VerifierQualityAnalyticsService verifierQualityAnalyticsService;
    private final InstitutionBehavioralLearningService institutionBehavioralLearningService;
    private final LearningDatasetExportService exportService;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;

    public AdminLearningController(CalibrationEngineService calibrationEngineService,
                                   FeedbackIntelligenceService feedbackIntelligenceService,
                                   VerifierQualityAnalyticsService verifierQualityAnalyticsService,
                                   InstitutionBehavioralLearningService institutionBehavioralLearningService,
                                   LearningDatasetExportService exportService,
                                   UserRepository userRepository,
                                   InstitutionRepository institutionRepository) {
        this.calibrationEngineService = calibrationEngineService;
        this.feedbackIntelligenceService = feedbackIntelligenceService;
        this.verifierQualityAnalyticsService = verifierQualityAnalyticsService;
        this.institutionBehavioralLearningService = institutionBehavioralLearningService;
        this.exportService = exportService;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> triggerManualAggregation() {
        Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = Instant.now();
        
        feedbackIntelligenceService.aggregateRulePerformance(start, end);
        feedbackIntelligenceService.aggregateRecommendationPerformance(start, end);
        calibrationEngineService.evaluateCalibration();
        verifierQualityAnalyticsService.calculateVerifierMetrics(start, end);
        institutionBehavioralLearningService.analyzeTrends(start, end);

        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Triggered baseline metrics aggregation and calibration evaluation successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calibration/proposals")
    public ResponseEntity<List<CalibrationProposalDTO>> getPendingProposals() {
        List<CalibrationProposal> proposals = calibrationEngineService.evaluateCalibration();
        List<CalibrationProposalDTO> dtos = proposals.stream().map(p -> {
            CalibrationProposalDTO dto = new CalibrationProposalDTO();
            dto.setId(p.getId());
            dto.setTargetParameter(p.getTargetParameter());
            dto.setParameterType(p.getParameterType());
            dto.setPreviousValue(p.getPreviousValue());
            dto.setProposedValue(p.getProposedValue());
            dto.setJustification(p.getJustification());
            dto.setStatus(p.getStatus().name());
            dto.setProposalConfidence(p.getProposalConfidence());
            dto.setSampleSize(p.getSampleSize());
            dto.setOverrideRate(p.getOverrideRate());
            dto.setPrecisionRate(p.getPrecisionRate());
            dto.setCalibrationCount(p.getCalibrationCount());
            dto.setLastCalibrationAt(p.getLastCalibrationAt());
            dto.setCreatedAt(p.getCreatedAt());
            if (p.getDecisionBy() != null) {
                userRepository.findById(p.getDecisionBy())
                        .ifPresent(u -> dto.setDecisionByUsername(u.getUsername()));
            }
            dto.setDecisionAt(p.getDecisionAt());
            dto.setRejectionReason(p.getRejectionReason());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/calibration/proposals/{id}/apply")
    public ResponseEntity<Void> applyProposal(@PathVariable("id") UUID proposalId) {
        UUID adminId = SecurityUtil.getCurrentUserId();
        calibrationEngineService.applyProposal(proposalId, adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/calibration/proposals/{id}/reject")
    public ResponseEntity<Void> rejectProposal(@PathVariable("id") UUID proposalId, @RequestParam("reason") String reason) {
        UUID adminId = SecurityUtil.getCurrentUserId();
        calibrationEngineService.rejectProposal(proposalId, adminId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rules/performance")
    public ResponseEntity<List<RulePerformanceDTO>> getRulePerformance() {
        Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
        List<RulePerformanceMetric> metrics = feedbackIntelligenceService.aggregateRulePerformance(start, Instant.now());
        List<RulePerformanceDTO> dtos = metrics.stream().map(m -> {
            RulePerformanceDTO dto = new RulePerformanceDTO();
            dto.setRuleName(m.getRuleName());
            dto.setTargetType(m.getTargetType());
            dto.setTotalEvaluations(m.getTotalEvaluations());
            dto.setOverrideCount(m.getOverrideCount());
            dto.setTruePositives(m.getTruePositives());
            dto.setFalsePositives(m.getFalsePositives());
            dto.setFalseNegatives(m.getFalseNegatives());
            dto.setPrecisionRate(m.getPrecisionRate());
            dto.setOverrideRate(m.getOverrideRate());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/rules/mistakes")
    public ResponseEntity<List<String>> getSystematicMistakes() {
        return ResponseEntity.ok(feedbackIntelligenceService.detectSystematicMistakes());
    }

    @GetMapping("/verifiers/quality")
    public ResponseEntity<List<VerifierQualityDTO>> getVerifierQuality() {
        Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
        List<VerifierQualityMetrics> metrics = verifierQualityAnalyticsService.calculateVerifierMetrics(start, Instant.now());
        List<VerifierQualityDTO> dtos = metrics.stream().map(m -> {
            VerifierQualityDTO dto = new VerifierQualityDTO();
            dto.setVerifierId(m.getVerifierId());
            userRepository.findById(m.getVerifierId())
                    .ifPresent(u -> dto.setVerifierUsername(u.getUsername()));
            dto.setTotalReviews(m.getTotalReviews());
            dto.setOverrideCount(m.getOverrideCount());
            dto.setOverrideRate(m.getOverrideRate());
            dto.setAverageReviewTimeSeconds(m.getAverageReviewTimeSeconds());
            dto.setConsensusAgreementRate(m.getConsensusAgreementRate());
            dto.setDecisionVariance(m.getDecisionVariance());
            dto.setRubberStampingRisk(m.getRubberStampingRisk());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/institutions/trends")
    public ResponseEntity<List<TrendAlertDTO>> getInstitutionTrends() {
        Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
        List<InstitutionBehavioralTrend> trends = institutionBehavioralLearningService.analyzeTrends(start, Instant.now());
        List<TrendAlertDTO> dtos = trends.stream().map(t -> {
            TrendAlertDTO dto = new TrendAlertDTO();
            dto.setInstitutionId(t.getInstitutionId());
            institutionRepository.findById(t.getInstitutionId())
                    .ifPresent(i -> dto.setInstitutionName(i.getName()));
            dto.setTotalVerifications(t.getTotalVerifications());
            dto.setOverrideRate(t.getOverrideRate());
            dto.setTrendType(t.getTrendType().name());
            dto.setDriftSeverity(t.getDriftSeverity().name());
            dto.setFraudSignalsCount(t.getFraudSignalsCount());
            dto.setAverageConfidenceScore(t.getAverageConfidenceScore());
            dto.setLastDeviationDetectedAt(t.getLastDeviationDetectedAt());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Export Endpoints

    @GetMapping("/export/signals")
    public ResponseEntity<String> exportSignals(@RequestParam(value = "format", defaultValue = "csv") String format) {
        Instant start = Instant.now().minus(365, ChronoUnit.DAYS);
        String data = "json".equalsIgnoreCase(format) ? exportService.exportSignalsAsJson(start, Instant.now()) : exportService.exportSignalsAsCsv(start, Instant.now());
        String contentType = "json".equalsIgnoreCase(format) ? MediaType.APPLICATION_JSON_VALUE : "text/csv";
        String filename = "reviewer_learning_signals." + ("json".equalsIgnoreCase(format) ? "json" : "csv");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @GetMapping("/export/rules")
    public ResponseEntity<String> exportRules(@RequestParam(value = "format", defaultValue = "csv") String format) {
        String data = "json".equalsIgnoreCase(format) ? exportService.exportRulePerformanceAsJson() : exportService.exportRulePerformanceAsCsv();
        String contentType = "json".equalsIgnoreCase(format) ? MediaType.APPLICATION_JSON_VALUE : "text/csv";
        String filename = "rule_performance_metrics." + ("json".equalsIgnoreCase(format) ? "json" : "csv");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @GetMapping("/export/trends")
    public ResponseEntity<String> exportTrends(@RequestParam(value = "format", defaultValue = "csv") String format) {
        String data = "json".equalsIgnoreCase(format) ? exportService.exportTrendsAsJson() : exportService.exportTrendsAsCsv();
        String contentType = "json".equalsIgnoreCase(format) ? MediaType.APPLICATION_JSON_VALUE : "text/csv";
        String filename = "institution_behavioral_trends." + ("json".equalsIgnoreCase(format) ? "json" : "csv");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}
