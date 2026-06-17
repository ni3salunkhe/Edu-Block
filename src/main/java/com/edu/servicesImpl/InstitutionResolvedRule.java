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
public class InstitutionResolvedRule implements VerificationRule {

    @Override
    public String getName() {
        return "InstitutionResolvedRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 1;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        boolean resolved = context.getResolvedInstitution().isPresent();
        if (resolved) {
            String instName = context.getResolvedInstitution().get().getName();
            return new RuleResult(true, dbConfiguredModifier, "Institution resolved successfully: " + instName);
        }
        return new RuleResult(false, 0, "Institution was not resolved");
    }
}
