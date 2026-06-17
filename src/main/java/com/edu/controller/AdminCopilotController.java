package com.edu.controller;

import com.edu.entity.ReviewerFeedback;
import com.edu.entity.ReviewerRecommendation;
import com.edu.repositories.ReviewerFeedbackRepository;
import com.edu.repositories.ReviewerRecommendationRepository;
import com.edu.services.ReviewerCopilotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/copilot")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCopilotController {

    private final ReviewerCopilotService copilotService;
    private final ReviewerRecommendationRepository recommendationRepository;
    private final ReviewerFeedbackRepository feedbackRepository;

    public AdminCopilotController(ReviewerCopilotService copilotService,
                                  ReviewerRecommendationRepository recommendationRepository,
                                  ReviewerFeedbackRepository feedbackRepository) {
        this.copilotService = copilotService;
        this.recommendationRepository = recommendationRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(copilotService.getCopilotStatistics());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ReviewerRecommendation>> getRecommendations() {
        return ResponseEntity.ok(recommendationRepository.findAll());
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<ReviewerFeedback>> getFeedback() {
        return ResponseEntity.ok(feedbackRepository.findAll());
    }

    // (D) Drift analytics endpoint — AI-5 backbone
    @GetMapping("/drift")
    public ResponseEntity<Map<String, Object>> getDriftAnalytics() {
        return ResponseEntity.ok(copilotService.getDriftAnalytics());
    }
}
