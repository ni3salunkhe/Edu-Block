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
public class StudentNamePresentRule implements VerificationRule {

    @Override
    public String getName() {
        return "StudentNamePresentRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 5;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        if (intel != null && intel.getStudentName() != null && !intel.getStudentName().trim().isEmpty()) {
            return new RuleResult(true, dbConfiguredModifier, "Student name successfully extracted: " + intel.getStudentName());
        }
        return new RuleResult(false, 0, "Student name not present");
    }
}
