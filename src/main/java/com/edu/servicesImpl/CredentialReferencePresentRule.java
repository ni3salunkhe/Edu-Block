package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.services.VerificationContext;
import com.edu.services.VerificationRule;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CredentialReferencePresentRule implements VerificationRule {

    @Override
    public String getName() {
        return "CredentialReferencePresentRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 7;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        if (intel != null && intel.getCredentialReference() != null && !intel.getCredentialReference().trim().isEmpty()) {
            return new RuleResult(true, dbConfiguredModifier, "Credential reference successfully extracted: " + intel.getCredentialReference());
        }
        return new RuleResult(false, 0, "Credential reference not present");
    }
}
