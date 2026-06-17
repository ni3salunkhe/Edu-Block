package com.edu.edublock;

import com.edu.entity.Document;
import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentOcr;
import com.edu.entity.FraudSignal;
import com.edu.entity.VerificationRuleConfig;
import com.edu.repositories.VerificationRuleConfigRepository;
import com.edu.services.VerificationRule;
import com.edu.services.VerificationRuleEngine;
import com.edu.services.VerificationContext;
import com.edu.servicesImpl.VerificationRuleEngineImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class VerificationRuleEngineTest {

    private VerificationRuleConfigRepository configRepository;
    private ObjectMapper objectMapper;
    private VerificationRule mockRule1;
    private VerificationRule mockRule2;
    private VerificationContext mockContext;

    @BeforeEach
    public void setUp() {
        configRepository = Mockito.mock(VerificationRuleConfigRepository.class);
        objectMapper = new ObjectMapper();
        mockRule1 = Mockito.mock(VerificationRule.class);
        mockRule2 = Mockito.mock(VerificationRule.class);
        mockContext = Mockito.mock(VerificationContext.class);

        when(mockRule1.getName()).thenReturn("Rule1");
        when(mockRule1.getEvaluationOrder()).thenReturn(1);
        when(mockRule2.getName()).thenReturn("Rule2");
        when(mockRule2.getEvaluationOrder()).thenReturn(2);
    }

    @Test
    public void testRuleEngineSuccessAndClamping() {
        Document doc = new Document();
        DocumentIntelligence intel = new DocumentIntelligence();
        DocumentOcr ocr = new DocumentOcr();
        List<FraudSignal> signals = new ArrayList<>();

        // Setup mock configs from DB
        when(configRepository.findById("Rule1")).thenReturn(Optional.of(new VerificationRuleConfig("Rule1", 20, true)));
        when(configRepository.findById("Rule2")).thenReturn(Optional.of(new VerificationRuleConfig("Rule2", -80, true)));

        // Setup mock rule evaluations
        when(mockRule1.evaluate(any(), any(), any(), any(), any(), eq(20)))
                .thenReturn(new VerificationRule.RuleResult(true, 20, "Rule1 Triggered"));
        when(mockRule2.evaluate(any(), any(), any(), any(), any(), eq(-80)))
                .thenReturn(new VerificationRule.RuleResult(true, -80, "Rule2 Triggered"));

        List<VerificationRule> rulesList = Arrays.asList(mockRule1, mockRule2);
        VerificationRuleEngine engine = new VerificationRuleEngineImpl(rulesList, configRepository, objectMapper);

        VerificationRuleEngine.EngineResult result = engine.evaluateRules(doc, intel, ocr, signals, mockContext);

        // Score starts at 50, +20 -80 = -10, clamped to 0
        assertEquals(0, result.getFinalScore());
        assertNotNull(result.getExplanationsJson());
        assertTrue(result.getExplanationsJson().contains("Rule1 Triggered"));
        assertTrue(result.getExplanationsJson().contains("Rule2 Triggered"));
    }

    @Test
    public void testDisabledRuleSkipped() {
        Document doc = new Document();
        DocumentIntelligence intel = new DocumentIntelligence();
        DocumentOcr ocr = new DocumentOcr();
        List<FraudSignal> signals = new ArrayList<>();

        // Rule1 disabled
        when(configRepository.findById("Rule1")).thenReturn(Optional.of(new VerificationRuleConfig("Rule1", 20, false)));
        when(configRepository.findById("Rule2")).thenReturn(Optional.of(new VerificationRuleConfig("Rule2", 30, true)));

        when(mockRule2.evaluate(any(), any(), any(), any(), any(), eq(30)))
                .thenReturn(new VerificationRule.RuleResult(true, 30, "Rule2 Triggered"));

        List<VerificationRule> rulesList = Arrays.asList(mockRule1, mockRule2);
        VerificationRuleEngine engine = new VerificationRuleEngineImpl(rulesList, configRepository, objectMapper);

        VerificationRuleEngine.EngineResult result = engine.evaluateRules(doc, intel, ocr, signals, mockContext);

        // Score starts at 50, Rule1 skipped, Rule2 +30 = 80
        assertEquals(80, result.getFinalScore());
        assertTrue(result.getExplanationsJson().contains("Rule disabled"));
    }
}
