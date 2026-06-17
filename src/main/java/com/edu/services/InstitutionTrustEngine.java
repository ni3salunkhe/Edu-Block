package com.edu.services;

import com.edu.dto.TrustScoreBreakdownDTO;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionRiskEvent;
import com.edu.repositories.projections.InstitutionMetricsProjection;
import java.util.List;

public interface InstitutionTrustEngine {
    
    class TrustEngineResult {
        private final int score;
        private final TrustScoreBreakdownDTO breakdown;
        private final List<String> reasons;

        public TrustEngineResult(int score, TrustScoreBreakdownDTO breakdown, List<String> reasons) {
            this.score = score;
            this.breakdown = breakdown;
            this.reasons = reasons;
        }

        public int getScore() { return score; }
        public TrustScoreBreakdownDTO getBreakdown() { return breakdown; }
        public List<String> getReasons() { return reasons; }
    }

    TrustEngineResult calculateTrust(Institution inst, InstitutionMetricsProjection metrics, List<InstitutionRiskEvent> riskEvents);
}
