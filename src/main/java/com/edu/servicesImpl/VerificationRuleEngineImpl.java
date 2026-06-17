package com.edu.servicesImpl;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.entity.VerificationRuleConfig;
import com.edu.repositories.VerificationRuleConfigRepository;
import com.edu.services.VerificationRule;
import com.edu.services.VerificationRuleEngine;
import com.edu.services.VerificationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class VerificationRuleEngineImpl implements VerificationRuleEngine {

    private final List<VerificationRule> rules;
    private final VerificationRuleConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    // Default configuration map to auto-initialize DB if blank
    private static final Map<String, Integer> DEFAULT_MODIFIERS = new HashMap<>();
    static {
        DEFAULT_MODIFIERS.put("InstitutionResolvedRule", 20);
        DEFAULT_MODIFIERS.put("InstitutionUnresolvedRule", -20);
        DEFAULT_MODIFIERS.put("BlockchainAnchorRule", 15);
        DEFAULT_MODIFIERS.put("AccreditationRule", 10);
        DEFAULT_MODIFIERS.put("StudentNamePresentRule", 10);
        DEFAULT_MODIFIERS.put("EnrollmentNumberPresentRule", 10);
        DEFAULT_MODIFIERS.put("CredentialReferencePresentRule", 10);
        DEFAULT_MODIFIERS.put("StudentNameMissingRule", -15);
        DEFAULT_MODIFIERS.put("EnrollmentNumberMissingRule", -10);
        DEFAULT_MODIFIERS.put("OcrConfidenceRule", -15);
        DEFAULT_MODIFIERS.put("DuplicateSimilarityRule", -40);
        DEFAULT_MODIFIERS.put("CredentialReferenceReuseRule", -30);
        DEFAULT_MODIFIERS.put("DocumentTypeMismatchRule", -25);
        DEFAULT_MODIFIERS.put("FutureDateRule", -20);
        DEFAULT_MODIFIERS.put("MultipleNamesRule", -15);
        DEFAULT_MODIFIERS.put("LowTextDensityRule", -20);
    }

    public VerificationRuleEngineImpl(List<VerificationRule> rules,
                                      VerificationRuleConfigRepository configRepository,
                                      ObjectMapper objectMapper) {
        this.rules = new ArrayList<>(rules);
        this.rules.sort(Comparator.comparingInt(VerificationRule::getEvaluationOrder));
        this.configRepository = configRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public EngineResult evaluateRules(Document doc, DocumentIntelligence intel, DocumentOcr ocr, List<FraudSignal> activeSignals, VerificationContext context) {
        int score = 50; // Starting baseline
        List<Map<String, Object>> explanations = new ArrayList<>();

        for (VerificationRule rule : rules) {
            String ruleName = rule.getName();
            
            // 1. Fetch DB configuration, auto-initialize if not present
            VerificationRuleConfig config = configRepository.findById(ruleName)
                    .orElseGet(() -> {
                        int defaultMod = DEFAULT_MODIFIERS.getOrDefault(ruleName, 0);
                        VerificationRuleConfig newConfig = new VerificationRuleConfig(ruleName, defaultMod, true);
                        return configRepository.save(newConfig);
                    });

            if (!config.getEnabled()) {
                // Rule disabled, log explanation and skip
                Map<String, Object> explanation = new LinkedHashMap<>();
                explanation.put("ruleName", ruleName);
                explanation.put("scoreModifier", 0);
                explanation.put("description", "Rule disabled by system administrator");
                explanation.put("triggered", false);
                explanations.add(explanation);
                continue;
            }

            // 2. Evaluate Rule
            VerificationRule.RuleResult result = rule.evaluate(doc, intel, ocr, activeSignals, context, config.getScoreModifier());

            Map<String, Object> explanation = new LinkedHashMap<>();
            explanation.put("ruleName", ruleName);
            explanation.put("scoreModifier", result.isTriggered() ? result.getScoreModifier() : 0);
            explanation.put("description", result.getDescription());
            explanation.put("triggered", result.isTriggered());
            explanations.add(explanation);

            if (result.isTriggered()) {
                score += result.getScoreModifier();
            }
        }

        // 3. Clamp final score between 0 and 100
        int finalScore = Math.max(0, Math.min(100, score));
        String json = "[]";
        try {
            json = objectMapper.writeValueAsString(explanations);
        } catch (Exception ignored) {}

        return new EngineResult(finalScore, json);
    }
}
