package com.edu.servicesImpl;

import com.edu.dto.TrustScoreBreakdownDTO;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionRiskEvent;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.repositories.projections.InstitutionMetricsProjection;
import com.edu.services.InstitutionTrustEngine;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InstitutionTrustEngineImpl implements InstitutionTrustEngine {

    @Override
    public TrustEngineResult calculateTrust(Institution inst, InstitutionMetricsProjection metrics, List<InstitutionRiskEvent> riskEvents) {
        List<String> reasons = new ArrayList<>();
        
        // 1. Accreditation / Trust Level baseline
        int accreditationContribution = 40;
        if (inst.getTrustLevel() == InstitutionTrustLevel.ACCREDITED) {
            accreditationContribution = 60;
            reasons.add("Accredited institution");
        } else {
            reasons.add("Registered/verified standard baseline");
        }

        // 2. Metrics aggregation
        long totalIssued = metrics != null && metrics.getTotalIssued() != null ? metrics.getTotalIssued() : 0L;
        long totalVerified = metrics != null && metrics.getTotalVerified() != null ? metrics.getTotalVerified() : 0L;
        double avgConfidence = metrics != null && metrics.getAvgConfidence() != null ? metrics.getAvgConfidence() : 0.0;
        long totalSignals = metrics != null && metrics.getTotalSignals() != null ? metrics.getTotalSignals() : 0L;

        double successRate = totalIssued > 0 ? (double) totalVerified / totalIssued : 0.0;
        double fraudRate = totalIssued > 0 ? (double) totalSignals / totalIssued : 0.0;

        int verificationContribution = (int) Math.round(30.0 * successRate * (avgConfidence / 100.0));
        reasons.add(String.format("Verification success rate: %.1f%%", successRate * 100.0));
        reasons.add(String.format("Average verification confidence: %.1f%%", avgConfidence));

        // 3. Fraud Penalties
        double rawFraudPenalty = fraudRate * 300.0 + totalSignals * 0.5;
        int fraudPenalty = (int) Math.min(40, Math.round(rawFraudPenalty));
        if (fraudPenalty > 0) {
            reasons.add(String.format("Fraud penalty applied: -%d (fraud rate: %.1f%%)", fraudPenalty, fraudRate * 100.0));
        } else {
            reasons.add("Low fraud incidence");
        }

        // 4. Unresolved Risk Events
        int riskPenalty = 0;
        boolean accreditationRevoked = false;

        for (InstitutionRiskEvent event : riskEvents) {
            if (!event.getResolved()) {
                if (event.getEventType() == InstitutionRiskEvent.RiskEventType.ACCREDITATION_REVOKED) {
                    accreditationRevoked = true;
                    reasons.add("CRITICAL: Accreditation has been revoked");
                } else {
                    int eventPenalty = 0;
                    switch (event.getSeverity()) {
                        case CRITICAL:
                            eventPenalty = 30;
                            break;
                        case HIGH:
                            eventPenalty = 15;
                            break;
                        case MEDIUM:
                            eventPenalty = 5;
                            break;
                        case LOW:
                        default:
                            eventPenalty = 0;
                            break;
                    }
                    riskPenalty += eventPenalty;
                    reasons.add(String.format("Active risk event (%s) penalty: -%d", event.getEventType(), eventPenalty));
                }
            }
        }

        int finalScore = accreditationContribution + verificationContribution - fraudPenalty - riskPenalty;
        
        // Instant Override for Revocation
        if (accreditationRevoked) {
            finalScore = 0;
            accreditationContribution = 0;
            verificationContribution = 0;
            fraudPenalty = 0;
            riskPenalty = 100;
        }

        finalScore = Math.max(0, Math.min(100, finalScore));

        TrustScoreBreakdownDTO breakdown = new TrustScoreBreakdownDTO(
                accreditationContribution,
                verificationContribution,
                fraudPenalty,
                riskPenalty,
                finalScore,
                Instant.now()
        );

        return new TrustEngineResult(finalScore, breakdown, reasons);
    }
}
