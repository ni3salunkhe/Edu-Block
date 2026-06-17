package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.entity.Institution;
import com.edu.entity.InstitutionTrustLevel;
import com.edu.services.VerificationContext;
import com.edu.services.VerificationRule;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AccreditationRule implements VerificationRule {

    @Override
    public String getName() {
        return "AccreditationRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 4;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        if (context.getResolvedInstitution().isPresent()) {
            Institution inst = context.getResolvedInstitution().get();
            if (inst.getTrustLevel() == InstitutionTrustLevel.ACCREDITED) {
                return new RuleResult(true, dbConfiguredModifier, "Resolved institution is accredited: " + inst.getName());
            }
        }
        return new RuleResult(false, 0, "Resolved institution is not accredited");
    }
}
