package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.services.VerificationContext;
import com.edu.services.VerificationRule;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OcrConfidenceRule implements VerificationRule {

    private static final Pattern CONFIDENCE_PATTERN = Pattern.compile("\"averageWordConfidence\"\\s*:\\s*([0-9.]+)");

    @Override
    public String getName() {
        return "OcrConfidenceRule";
    }

    @Override
    public int getEvaluationOrder() {
        return 10;
    }

    @Override
    public RuleResult evaluate(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context, int dbConfiguredModifier) {
        if (ocr != null && ocr.getOcrMetadataJson() != null) {
            Matcher m = CONFIDENCE_PATTERN.matcher(ocr.getOcrMetadataJson());
            if (m.find()) {
                try {
                    double confidence = Double.parseDouble(m.group(1));
                    if (confidence < 60.0) {
                        return new RuleResult(true, dbConfiguredModifier, "OCR recognition confidence low: " + confidence + "%");
                    }
                } catch (Exception ignored) {}
            }
        }
        return new RuleResult(false, 0, "OCR confidence within normal range");
    }
}
