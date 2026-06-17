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
public class CredentialReferenceReuseRule implements VerificationRule {

    @Override
    public String getName() {
        return "CredentialReferenceReuseRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 12;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        boolean reuse = activeSignals.stream().anyMatch(fs -> fs.getSignalType() == FraudSignalType.SAME_CREDENTIAL_REFERENCE_MULTIPLE_TIMES);
        if (reuse) {
            return new RuleResult(true, dbConfiguredModifier, "Credential reference is used across multiple distinct student accounts");
        }
        return new RuleResult(false, 0, "No duplicate credential reference usage detected");
    }
}
