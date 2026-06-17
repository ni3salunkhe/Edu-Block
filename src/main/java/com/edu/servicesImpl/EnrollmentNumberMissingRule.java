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
public class EnrollmentNumberMissingRule implements VerificationRule {

    @Override
    public String getName() {
        return "EnrollmentNumberMissingRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 9;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        if (intel == null || intel.getEnrollmentNumber() == null || intel.getEnrollmentNumber().trim().isEmpty()) {
            return new RuleResult(true, dbConfiguredModifier, "Missing enrollment number");
        }
        return new RuleResult(false, 0, "Enrollment number is present");
    }
}
