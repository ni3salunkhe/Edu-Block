package com.edu.services;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import java.util.List;

public interface VerificationRuleEngine {
    
    EngineResult evaluateRules(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context);

    class EngineResult {
        private final int finalScore;
        private final String explanationsJson;

        public EngineResult(int finalScore, String explanationsJson) {
            this.finalScore = finalScore;
            this.explanationsJson = explanationsJson;
        }

        public int getFinalScore() {
            return finalScore;
        }

        public String getExplanationsJson() {
            return explanationsJson;
        }
    }
}
