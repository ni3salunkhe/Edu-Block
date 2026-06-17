package com.edu.servicesImpl;

import com.edu.dto.TrustScoreBreakdownDTO;
import com.edu.entity.*;
import com.edu.repositories.*;
import com.edu.repositories.projections.InstitutionMetricsProjection;
import com.edu.services.InstitutionTrustEngine;
import com.edu.services.InstitutionTrustService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InstitutionTrustServiceImpl implements InstitutionTrustService {

    private final InstitutionRepository institutionRepository;
    private final InstitutionTrustProfileRepository trustProfileRepository;
    private final InstitutionTrustHistoryRepository trustHistoryRepository;
    private final InstitutionRiskEventRepository riskEventRepository;
    private final InstitutionTrustEngine trustEngine;

    // Lightweight lock registry for dynamic concurrency protection
    private final ConcurrentHashMap<UUID, Object> locks = new ConcurrentHashMap<>();

    public InstitutionTrustServiceImpl(InstitutionRepository institutionRepository,
                                       InstitutionTrustProfileRepository trustProfileRepository,
                                       InstitutionTrustHistoryRepository trustHistoryRepository,
                                       InstitutionRiskEventRepository riskEventRepository,
                                       InstitutionTrustEngine trustEngine) {
        this.institutionRepository = institutionRepository;
        this.trustProfileRepository = trustProfileRepository;
        this.trustHistoryRepository = trustHistoryRepository;
        this.riskEventRepository = riskEventRepository;
        this.trustEngine = trustEngine;
    }

    @Override
    @Transactional
    public InstitutionTrustProfile getOrCreateProfile(UUID institutionId) {
        return trustProfileRepository.findById(institutionId)
                .orElseGet(() -> {
                    Institution inst = institutionRepository.findById(institutionId)
                            .orElseThrow(() -> new IllegalArgumentException("Institution not found for ID: " + institutionId));
                    
                    InstitutionTrustProfile profile = new InstitutionTrustProfile();
                    profile.setInstitutionId(institutionId);
                    profile.setInstitution(inst);
                    profile.setTrustScore(50);
                    profile.setTrustTier(InstitutionTrustProfile.TrustTier.MEDIUM);
                    profile.setCalculationVersion("AI3_v1");
                    profile.setMetricsWindowStart(Instant.now().minus(30, ChronoUnit.DAYS));
                    profile.setMetricsWindowEnd(Instant.now());
                    profile.setFraudRate(0.0);
                    profile.setVerificationSuccessRate(0.0);
                    profile.setAverageConfidenceScore(0.0);
                    profile.setTotalIssuedCredentials(0L);
                    profile.setTotalVerifiedCredentials(0L);
                    profile.setTotalFraudSignals(0L);
                    profile.setTrustReasons(Collections.singletonList("Initial trust profile generated"));
                    profile.setScoreBreakdown(new TrustScoreBreakdownDTO(40, 10, 0, 0, 50, Instant.now()));
                    profile.setLastCalculatedAt(Instant.now());
                    
                    return trustProfileRepository.save(profile);
                });
    }

    @Override
    @Transactional
    public InstitutionTrustProfile recalculateTrustProfile(UUID institutionId, String changeTriggerType, String reason) {
        Object lock = locks.computeIfAbsent(institutionId, k -> new Object());
        synchronized (lock) {
            Institution inst = institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new IllegalArgumentException("Institution not found for ID: " + institutionId));

            InstitutionMetricsProjection metrics = trustProfileRepository.aggregateInstitutionMetrics(institutionId);
            List<InstitutionRiskEvent> riskEvents = riskEventRepository.findByInstitutionIdAndResolvedFalse(institutionId);

            InstitutionTrustEngine.TrustEngineResult result = trustEngine.calculateTrust(inst, metrics, riskEvents);

            InstitutionTrustProfile profile = getOrCreateProfile(institutionId);
            int previousScore = profile.getTrustScore();
            int newScore = result.getScore();

            if (previousScore != newScore) {
                InstitutionTrustHistory history = new InstitutionTrustHistory(
                        institutionId,
                        previousScore,
                        newScore,
                        newScore - previousScore,
                        changeTriggerType,
                        reason,
                        Instant.now()
                );
                trustHistoryRepository.save(history);
            }

            profile.setTrustScore(newScore);
            
            // Map scores to tiers
            InstitutionTrustProfile.TrustTier tier;
            if (newScore >= 90) tier = InstitutionTrustProfile.TrustTier.VERY_HIGH;
            else if (newScore >= 75) tier = InstitutionTrustProfile.TrustTier.HIGH;
            else if (newScore >= 50) tier = InstitutionTrustProfile.TrustTier.MEDIUM;
            else if (newScore >= 25) tier = InstitutionTrustProfile.TrustTier.LOW;
            else tier = InstitutionTrustProfile.TrustTier.CRITICAL;
            profile.setTrustTier(tier);

            profile.setTotalIssuedCredentials(metrics != null && metrics.getTotalIssued() != null ? metrics.getTotalIssued() : 0L);
            profile.setTotalVerifiedCredentials(metrics != null && metrics.getTotalVerified() != null ? metrics.getTotalVerified() : 0L);
            profile.setAverageConfidenceScore(metrics != null && metrics.getAvgConfidence() != null ? metrics.getAvgConfidence() : 0.0);
            profile.setTotalFraudSignals(metrics != null && metrics.getTotalSignals() != null ? metrics.getTotalSignals() : 0L);

            double successRate = profile.getTotalIssuedCredentials() > 0 
                ? (double) profile.getTotalVerifiedCredentials() / profile.getTotalIssuedCredentials() 
                : 0.0;
            double fraudRate = profile.getTotalIssuedCredentials() > 0 
                ? (double) profile.getTotalFraudSignals() / profile.getTotalIssuedCredentials() 
                : 0.0;
            profile.setVerificationSuccessRate(successRate);
            profile.setFraudRate(fraudRate);

            profile.setTrustReasons(result.getReasons());
            profile.setScoreBreakdown(result.getBreakdown());
            profile.setMetricsWindowEnd(Instant.now());
            profile.setLastCalculatedAt(Instant.now());

            return trustProfileRepository.save(profile);
        }
    }

    @Override
    @Transactional
    public void handleVerificationSuccess(UUID institutionId, int confidenceScore) {
        recalculateTrustProfile(
                institutionId,
                "VERIFICATION_SUCCESS",
                "Verification completed successfully with score: " + confidenceScore
        );
    }

    @Override
    @Transactional
    public void handleFraudSignal(UUID institutionId, String signalType, String severity) {
        // Only trigger risk events and penalty score reductions for HIGH and CRITICAL severity signals
        if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
            InstitutionRiskEvent.RiskEventSeverity rSeverity = "CRITICAL".equalsIgnoreCase(severity) 
                    ? InstitutionRiskEvent.RiskEventSeverity.CRITICAL 
                    : InstitutionRiskEvent.RiskEventSeverity.HIGH;

            InstitutionRiskEvent event = new InstitutionRiskEvent(
                    institutionId,
                    InstitutionRiskEvent.RiskEventType.FRAUD_SPIKE,
                    rSeverity,
                    "Fraud signal detected: " + signalType,
                    "{\"signalType\":\"" + signalType + "\",\"severity\":\"" + severity + "\"}",
                    Instant.now(),
                    Instant.now().plus(30, ChronoUnit.DAYS), // expires after 30 days
                    false
            );
            riskEventRepository.save(event);

            recalculateTrustProfile(
                    institutionId,
                    "FRAUD_SIGNAL",
                    "Severe fraud signal detected: " + signalType + " (" + severity + ")"
            );
        }
    }

    @Override
    @Transactional
    public void handleAccreditationChange(UUID institutionId, String newLevel) {
        Institution inst = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found for ID: " + institutionId));

        if ("ACCREDITATION_REVOKED".equalsIgnoreCase(newLevel)) {
            InstitutionRiskEvent event = new InstitutionRiskEvent(
                    institutionId,
                    InstitutionRiskEvent.RiskEventType.ACCREDITATION_REVOKED,
                    InstitutionRiskEvent.RiskEventSeverity.CRITICAL,
                    "Accreditation status revoked",
                    "{}",
                    Instant.now(),
                    null,
                    false
            );
            riskEventRepository.save(event);
        }

        recalculateTrustProfile(
                institutionId,
                "ACCREDITATION_CHANGE",
                "Accreditation status changed to: " + newLevel
        );
    }

    @Override
    @Transactional
    public void handleCredentialIssued(UUID institutionId) {
        recalculateTrustProfile(
                institutionId,
                "CREDENTIAL_ISSUED",
                "New credential issued by institution"
        );
    }
}
