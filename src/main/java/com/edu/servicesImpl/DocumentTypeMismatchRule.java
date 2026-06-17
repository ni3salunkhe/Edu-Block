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
public class DocumentTypeMismatchRule implements VerificationRule {

    @Override
    public String getName() {
        return "DocumentTypeMismatchRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 13;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        boolean mismatch = activeSignals.stream().anyMatch(fs -> fs.getSignalType() == FraudSignalType.DOCUMENT_TYPE_MISMATCH);
        if (mismatch) {
            return new RuleResult(true, dbConfiguredModifier, "Uploaded document type does not match OCR classification");
        }
        return new RuleResult(false, 0, "No document type mismatch detected");
    }
}
