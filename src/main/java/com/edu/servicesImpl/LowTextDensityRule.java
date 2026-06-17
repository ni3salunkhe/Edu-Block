package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.entity.FraudSignal.FraudSignalType;
import com.edu.services.VerificationContext;
import com.edu.services.VerificationRule;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LowTextDensityRule implements VerificationRule {

    @Override
    public String getName() {
        return "LowTextDensityRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 16;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        boolean lowDensity = activeSignals.stream().anyMatch(fs -> fs.getSignalType() == FraudSignalType.LOW_TEXT_DENSITY);
        if (lowDensity) {
            return new RuleResult(true, dbConfiguredModifier, "Extremely low text density detected (potential image/fragment tampering)");
        }
        return new RuleResult(false, 0, "Text density meets validation thresholds");
    }
}
