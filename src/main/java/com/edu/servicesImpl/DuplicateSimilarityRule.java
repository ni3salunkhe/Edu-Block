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
public class DuplicateSimilarityRule implements VerificationRule {

    @Override
    public String getName() {
        return "DuplicateSimilarityRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 11;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        boolean duplicate = activeSignals.stream().anyMatch(fs -> fs.getSignalType() == FraudSignalType.DUPLICATE_CERTIFICATE);
        if (duplicate) {
            return new RuleResult(true, dbConfiguredModifier, "Visual similarity check flagged duplicate document match");
        }
        return new RuleResult(false, 0, "No duplicate document detected");
    }
}
