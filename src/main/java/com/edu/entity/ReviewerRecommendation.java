package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.edu.dto.EvidenceSnapshotDTO;
import com.edu.dto.RecommendationScoreBreakdownDTO;
import com.edu.util.StringListConverter;
import com.edu.util.EvidenceSnapshotConverter;
import com.edu.util.RecommendationScoreBreakdownConverter;

@Entity
@Table(name = "reviewer_recommendations", indexes = {
    @Index(name = "idx_rec_doc_state", columnList = "document_id, state"),
    @Index(name = "idx_rec_priority", columnList = "review_priority DESC")
})
public class ReviewerRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "generation_number", nullable = false)
    private Integer generationNumber; // Increments per refresh/re-evaluation for the document

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecommendationState state; // ACTIVE, EXPIRED, SUPERSEDED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecommendationType recommendation; // APPROVE, REVIEW, REJECT

    @Column(nullable = false)
    private Integer recommendationScore; // 0 to 100

    @Convert(converter = RecommendationScoreBreakdownConverter.class)
    @Column(name = "score_breakdown", nullable = false, columnDefinition = "TEXT")
    private RecommendationScoreBreakdownDTO scoreBreakdown; // Detailed scoring contributions

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecommendationConfidence confidence; // VERY_HIGH, HIGH, MEDIUM, LOW

    @Column(nullable = false)
    private Boolean contradictorySignals; // True when AI-2 and AI-3 components heavily conflict

    @Column(name = "review_priority", nullable = false)
    private Integer reviewPriority; // 0 to 100, maps how urgently a human needs to look at it

    @Column(nullable = false)
    private Instant generatedAt;

    @Column(nullable = false)
    private Instant expiresAt; // Hard freshness window (e.g., generatedAt + 30 days)

    @Column(nullable = false, length = 20)
    private String modelVersion; // "AI4_v1"

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> explanationSummary;

    @Convert(converter = EvidenceSnapshotConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private EvidenceSnapshotDTO evidenceSnapshot; // Auditing exact stats at calculation time

    @Column(name = "generation_latency_ms")
    private Long generationLatencyMs; // Performance tracking metrics

    public enum RecommendationState {
        ACTIVE,
        EXPIRED,
        SUPERSEDED
    }

    public enum RecommendationType {
        APPROVE,
        REVIEW,
        REJECT
    }

    public enum RecommendationConfidence {
        VERY_HIGH,
        HIGH,
        MEDIUM,
        LOW
    }

    public ReviewerRecommendation() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public Integer getGenerationNumber() {
        return generationNumber;
    }

    public void setGenerationNumber(Integer generationNumber) {
        this.generationNumber = generationNumber;
    }

    public RecommendationState getState() {
        return state;
    }

    public void setState(RecommendationState state) {
        this.state = state;
    }

    public RecommendationType getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(RecommendationType recommendation) {
        this.recommendation = recommendation;
    }

    public Integer getRecommendationScore() {
        return recommendationScore;
    }

    public void setRecommendationScore(Integer recommendationScore) {
        this.recommendationScore = recommendationScore;
    }

    public RecommendationScoreBreakdownDTO getScoreBreakdown() {
        return scoreBreakdown;
    }

    public void setScoreBreakdown(RecommendationScoreBreakdownDTO scoreBreakdown) {
        this.scoreBreakdown = scoreBreakdown;
    }

    public RecommendationConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(RecommendationConfidence confidence) {
        this.confidence = confidence;
    }

    public Boolean getContradictorySignals() {
        return contradictorySignals;
    }

    public void setContradictorySignals(Boolean contradictorySignals) {
        this.contradictorySignals = contradictorySignals;
    }

    public Integer getReviewPriority() {
        return reviewPriority;
    }

    public void setReviewPriority(Integer reviewPriority) {
        this.reviewPriority = reviewPriority;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public List<String> getExplanationSummary() {
        return explanationSummary;
    }

    public void setExplanationSummary(List<String> explanationSummary) {
        this.explanationSummary = explanationSummary;
    }

    public EvidenceSnapshotDTO getEvidenceSnapshot() {
        return evidenceSnapshot;
    }

    public void setEvidenceSnapshot(EvidenceSnapshotDTO evidenceSnapshot) {
        this.evidenceSnapshot = evidenceSnapshot;
    }

    public Long getGenerationLatencyMs() {
        return generationLatencyMs;
    }

    public void setGenerationLatencyMs(Long generationLatencyMs) {
        this.generationLatencyMs = generationLatencyMs;
    }
}
