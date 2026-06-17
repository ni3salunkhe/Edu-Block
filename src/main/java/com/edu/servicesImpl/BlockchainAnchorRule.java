package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.DocumentSource;
import com.edu.entity.FraudSignal;
import com.edu.services.VerificationContext;
import com.edu.services.VerificationRule;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BlockchainAnchorRule implements VerificationRule {

    @Override
    public String getName() {
        return "BlockchainAnchorRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 3;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        // Source is Institution Issued (anchored on blockchain)
        if (doc.getSource() == DocumentSource.INSTITUTION_ISSUED) {
            return new RuleResult(true, dbConfiguredModifier, "Document is anchored via official institution issuance pipeline");
        }
        return new RuleResult(false, 0, "Document uploaded by individual user");
    }
}
