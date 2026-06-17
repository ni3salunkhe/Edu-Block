package com.edu.edublock;

import com.edu.dto.TrustScoreBreakdownDTO;
import com.edu.entity.*;
import com.edu.repositories.projections.InstitutionMetricsProjection;
import com.edu.services.InstitutionTrustEngine;
import com.edu.servicesImpl.InstitutionTrustEngineImpl;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InstitutionTrustEngineTest {

    private final InstitutionTrustEngine trustEngine = new InstitutionTrustEngineImpl();

    @Test
    public void testAccreditedBaselineScoring() {
        Institution inst = new Institution();
        inst.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        InstitutionMetricsProjection metrics = new InstitutionMetricsProjection() {
            @Override public Long getTotalIssued() { return 10L; }
            @Override public Long getTotalVerified() { return 10L; }
            @Override public Double getAvgConfidence() { return 100.0; }
            @Override public Long getTotalSignals() { return 0L; }
        };

        List<InstitutionRiskEvent> riskEvents = new ArrayList<>();

        InstitutionTrustEngine.TrustEngineResult result = trustEngine.calculateTrust(inst, metrics, riskEvents);

        assertEquals(90, result.getScore()); // 60 + 30 - 0 = 90
        TrustScoreBreakdownDTO breakdown = result.getBreakdown();
        assertEquals(60, breakdown.getAccreditationContribution());
        assertEquals(30, breakdown.getVerificationContribution());
        assertEquals(0, breakdown.getFraudPenalty());
        assertEquals(0, breakdown.getRiskPenalty());
    }

    @Test
    public void testNonAccreditedScoring() {
        Institution inst = new Institution();
        inst.setTrustLevel(InstitutionTrustLevel.VERIFIED);

        InstitutionMetricsProjection metrics = new InstitutionMetricsProjection() {
            @Override public Long getTotalIssued() { return 100L; }
            @Override public Long getTotalVerified() { return 80L; }
            @Override public Double getAvgConfidence() { return 90.0; }
            @Override public Long getTotalSignals() { return 10L; }
        };

        List<InstitutionRiskEvent> riskEvents = new ArrayList<>();

        InstitutionTrustEngine.TrustEngineResult result = trustEngine.calculateTrust(inst, metrics, riskEvents);

        // accreditation baseline = 40
        // success rate = 80/100 = 0.8
        // avg confidence = 90
        // verification contribution = 30 * 0.8 * (90/100) = 21.6 -> 22
        // fraud rate = 10/100 = 0.1
        // totalSignals = 10
        // raw fraud penalty = 0.1 * 300 + 10 * 0.5 = 30 + 5 = 35 -> 35
        // expected score = 40 + 22 - 35 - 0 = 27
        assertEquals(27, result.getScore());
    }

    @Test
    public void testAccreditationRevocationOverride() {
        Institution inst = new Institution();
        inst.setTrustLevel(InstitutionTrustLevel.ACCREDITED);

        InstitutionMetricsProjection metrics = new InstitutionMetricsProjection() {
            @Override public Long getTotalIssued() { return 10L; }
            @Override public Long getTotalVerified() { return 10L; }
            @Override public Double getAvgConfidence() { return 100.0; }
            @Override public Long getTotalSignals() { return 0L; }
        };

        List<InstitutionRiskEvent> riskEvents = new ArrayList<>();
        riskEvents.add(new InstitutionRiskEvent(
                inst.getId(),
                InstitutionRiskEvent.RiskEventType.ACCREDITATION_REVOKED,
                InstitutionRiskEvent.RiskEventSeverity.CRITICAL,
                "Accreditation revoked",
                "{}",
                Instant.now(),
                null,
                false
        ));

        InstitutionTrustEngine.TrustEngineResult result = trustEngine.calculateTrust(inst, metrics, riskEvents);

        assertEquals(0, result.getScore());
        TrustScoreBreakdownDTO breakdown = result.getBreakdown();
        assertEquals(0, breakdown.getAccreditationContribution());
        assertEquals(0, breakdown.getVerificationContribution());
        assertEquals(100, breakdown.getRiskPenalty());
    }
}
