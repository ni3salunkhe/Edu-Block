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
public class FutureDateRule implements VerificationRule {

    @Override
    public String getName() {
        return "FutureDateRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 14;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        boolean futureDate = activeSignals.stream().anyMatch(fs -> fs.getSignalType() == FraudSignalType.FUTURE_DATE_DETECTED);
        if (futureDate) {
            return new RuleResult(true, dbConfiguredModifier, "Document graduation or issue date is set in the future");
        }
        return new RuleResult(false, 0, "No future dates detected");
    }
}
