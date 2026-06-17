package com.edu.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.edu.dto.TrustScoreBreakdownDTO;
import com.edu.util.StringListConverter;
import com.edu.util.TrustScoreBreakdownConverter;

import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "institution_trust_profiles")
public class InstitutionTrustProfile implements Persistable<UUID> {

    @Id
    private UUID institutionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @Column(nullable = false)
    private Integer trustScore; // 0 to 100

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrustTier trustTier; // VERY_HIGH, HIGH, MEDIUM, LOW, CRITICAL

    @Column(nullable = false, length = 20)
    private String calculationVersion; // Snapshot tracking version (e.g., "AI3_v1")

    @Column(nullable = false)
    private Instant metricsWindowStart;

    @Column(nullable = false)
    private Instant metricsWindowEnd;

    @Column(nullable = false)
    private Double fraudRate;

    @Column(nullable = false)
    private Double verificationSuccessRate;

    @Column(nullable = false)
    private Double averageConfidenceScore;

    @Column(nullable = false)
    private Long totalIssuedCredentials;

    @Column(nullable = false)
    private Long totalVerifiedCredentials;

    @Column(nullable = false)
    private Long totalFraudSignals;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> trustReasons; // Structured explanation list

    @Convert(converter = TrustScoreBreakdownConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private TrustScoreBreakdownDTO scoreBreakdown; // Detailed scoring breakdown

    @Column(nullable = false)
    private Instant lastCalculatedAt;

    public enum TrustTier {
        VERY_HIGH, // Score 90-100
        HIGH,      // Score 75-89
        MEDIUM,    // Score 50-74
        LOW,       // Score 25-49
        CRITICAL   // Score 0-24
    }

    public InstitutionTrustProfile() {}

    public InstitutionTrustProfile(UUID institutionId, Institution institution, Integer trustScore, TrustTier trustTier, String calculationVersion, Instant metricsWindowStart, Instant metricsWindowEnd, Double fraudRate, Double verificationSuccessRate, Double averageConfidenceScore, Long totalIssuedCredentials, Long totalVerifiedCredentials, Long totalFraudSignals, List<String> trustReasons, TrustScoreBreakdownDTO scoreBreakdown, Instant lastCalculatedAt) {
        this.institutionId = institutionId;
        this.institution = institution;
        this.trustScore = trustScore;
        this.trustTier = trustTier;
        this.calculationVersion = calculationVersion;
        this.metricsWindowStart = metricsWindowStart;
        this.metricsWindowEnd = metricsWindowEnd;
        this.fraudRate = fraudRate;
        this.verificationSuccessRate = verificationSuccessRate;
        this.averageConfidenceScore = averageConfidenceScore;
        this.totalIssuedCredentials = totalIssuedCredentials;
        this.totalVerifiedCredentials = totalVerifiedCredentials;
        this.totalFraudSignals = totalFraudSignals;
        this.trustReasons = trustReasons;
        this.scoreBreakdown = scoreBreakdown;
        this.lastCalculatedAt = lastCalculatedAt;
    }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public Integer getTrustScore() { return trustScore; }
    public void setTrustScore(Integer trustScore) { this.trustScore = trustScore; }

    public TrustTier getTrustTier() { return trustTier; }
    public void setTrustTier(TrustTier trustTier) { this.trustTier = trustTier; }

    public String getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(String calculationVersion) { this.calculationVersion = calculationVersion; }

    public Instant getMetricsWindowStart() { return metricsWindowStart; }
    public void setMetricsWindowStart(Instant metricsWindowStart) { this.metricsWindowStart = metricsWindowStart; }

    public Instant getMetricsWindowEnd() { return metricsWindowEnd; }
    public void setMetricsWindowEnd(Instant metricsWindowEnd) { this.metricsWindowEnd = metricsWindowEnd; }

    public Double getFraudRate() { return fraudRate; }
    public void setFraudRate(Double fraudRate) { this.fraudRate = fraudRate; }

    public Double getVerificationSuccessRate() { return verificationSuccessRate; }
    public void setVerificationSuccessRate(Double verificationSuccessRate) { this.verificationSuccessRate = verificationSuccessRate; }

    public Double getAverageConfidenceScore() { return averageConfidenceScore; }
    public void setAverageConfidenceScore(Double averageConfidenceScore) { this.averageConfidenceScore = averageConfidenceScore; }

    public Long getTotalIssuedCredentials() { return totalIssuedCredentials; }
    public void setTotalIssuedCredentials(Long totalIssuedCredentials) { this.totalIssuedCredentials = totalIssuedCredentials; }

    public Long getTotalVerifiedCredentials() { return totalVerifiedCredentials; }
    public void setTotalVerifiedCredentials(Long totalVerifiedCredentials) { this.totalVerifiedCredentials = totalVerifiedCredentials; }

    public Long getTotalFraudSignals() { return totalFraudSignals; }
    public void setTotalFraudSignals(Long totalFraudSignals) { this.totalFraudSignals = totalFraudSignals; }

    public List<String> getTrustReasons() { return trustReasons; }
    public void setTrustReasons(List<String> trustReasons) { this.trustReasons = trustReasons; }

    public TrustScoreBreakdownDTO getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(TrustScoreBreakdownDTO scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public Instant getLastCalculatedAt() { return lastCalculatedAt; }
    public void setLastCalculatedAt(Instant lastCalculatedAt) { this.lastCalculatedAt = lastCalculatedAt; }

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return this.institutionId;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNew = false;
    }
}
