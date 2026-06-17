package com.edu.services;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import java.util.List;

public interface VerificationRule {
    
    String getName();
    
    int getEvaluationOrder();
    
    RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier);

    class RuleResult {
        private final boolean triggered;
        private final int scoreModifier;
        private final String description;

        public RuleResult(boolean triggered, int scoreModifier, String description) {
            this.triggered = triggered;
            this.scoreModifier = scoreModifier;
            this.description = description;
        }

        public boolean isTriggered() {
            return triggered;
        }

        public int getScoreModifier() {
            return scoreModifier;
        }

        public String getDescription() {
            return description;
        }
    }
}
