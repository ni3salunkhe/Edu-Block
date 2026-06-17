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
public class MultipleNamesRule implements VerificationRule {

    @Override
    public String getName() {
        return "MultipleNamesRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 15;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        boolean multipleNames = activeSignals.stream().anyMatch(fs -> fs.getSignalType() == FraudSignalType.MULTIPLE_NAMES_DETECTED);
        if (multipleNames) {
            return new RuleResult(true, dbConfiguredModifier, "Multiple distinct proper candidate names found in document text");
        }
        return new RuleResult(false, 0, "No multiple candidate name triggers detected");
    }
}
