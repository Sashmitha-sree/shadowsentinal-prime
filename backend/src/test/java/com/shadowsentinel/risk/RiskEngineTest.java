package com.shadowsentinel.risk;

import com.shadowsentinel.browser.BrowserActivity;
import com.shadowsentinel.classification.ClassLabel;
import com.shadowsentinel.classification.ClassificationEvidence;
import com.shadowsentinel.classification.ClassificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskEngineTest {

    private RiskEngine riskEngine;

    @BeforeEach
    void setUp() {
        riskEngine = new RiskEngine();
    }

    @Test
    @DisplayName("Base score matches specification for each class label")
    void getBaseScore_MatchesSpec() {
        assertEquals(0, riskEngine.getBaseScore(ClassLabel.NON_AI));
        assertEquals(10, riskEngine.getBaseScore(ClassLabel.AI_CAPABLE_PAGE));
        assertEquals(25, riskEngine.getBaseScore(ClassLabel.AI_INTERACTION));
        assertEquals(40, riskEngine.getBaseScore(ClassLabel.AI_GENERATION));
        assertEquals(0, riskEngine.getBaseScore(null));
    }

    @ParameterizedTest(name = "Score {0} maps to RiskLevel {1}")
    @CsvSource({
            "0, LOW",
            "24, LOW",
            "25, MEDIUM",
            "49, MEDIUM",
            "50, HIGH",
            "74, HIGH",
            "75, CRITICAL",
            "100, CRITICAL"
    })
    @DisplayName("Exact boundary tests at 24/25/49/50/74/75")
    void determineRiskLevel_Boundaries(int score, RiskLevel expectedLevel) {
        assertEquals(expectedLevel, riskEngine.determineRiskLevel(score));
    }

    @Test
    @DisplayName("Glob domain matching handles wildcards and exact matches")
    void matchesGlob_Cases() {
        assertTrue(riskEngine.matchesGlob("chatgpt.com", "*chatgpt.com*"));
        assertTrue(riskEngine.matchesGlob("sub.chatgpt.com", "*chatgpt.com*"));
        assertTrue(riskEngine.matchesGlob("claude.ai", "*.ai"));
        assertFalse(riskEngine.matchesGlob("google.com", "*.ai"));
        assertTrue(riskEngine.matchesGlob("openai.com", "openai.com"));
        assertFalse(riskEngine.matchesGlob("sub.openai.com", "openai.com"));
        assertTrue(riskEngine.matchesGlob("anything.org", "*"));
    }

    @Test
    @DisplayName("Rule matches only when all non-null conditions are satisfied")
    void ruleMatching_Conditions() {
        BrowserActivity activity = BrowserActivity.builder().domain("chatgpt.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_GENERATION)
                .confidence(0.95)
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .fileUploadPresent(true)
                .pasteEventCount(2)
                .generateClickCount(6)
                .build();

        PolicyRule matchingRule = PolicyRule.builder()
                .ruleKey("TEST_MATCH")
                .appliesToLabel(ClassLabel.AI_GENERATION)
                .domainPattern("*chatgpt.com*")
                .minGenerateClicks(5)
                .requiresFileUpload(true)
                .requiresPasteEvent(true)
                .scoreWeight(20)
                .description("All conditions met")
                .build();

        assertTrue(riskEngine.matchesRule(matchingRule, result, evidence, "chatgpt.com"));

        // Label mismatch
        PolicyRule labelMismatch = PolicyRule.builder()
                .appliesToLabel(ClassLabel.NON_AI)
                .build();
        assertFalse(riskEngine.matchesRule(labelMismatch, result, evidence, "chatgpt.com"));

        // Domain mismatch
        PolicyRule domainMismatch = PolicyRule.builder()
                .domainPattern("claude.ai")
                .build();
        assertFalse(riskEngine.matchesRule(domainMismatch, result, evidence, "chatgpt.com"));

        // Generate click threshold mismatch
        PolicyRule clicksMismatch = PolicyRule.builder()
                .minGenerateClicks(10)
                .build();
        assertFalse(riskEngine.matchesRule(clicksMismatch, result, evidence, "chatgpt.com"));

        // File upload mismatch
        PolicyRule fileUploadMismatch = PolicyRule.builder()
                .requiresFileUpload(false)
                .build();
        assertFalse(riskEngine.matchesRule(fileUploadMismatch, result, evidence, "chatgpt.com"));

        // Paste event mismatch
        PolicyRule pasteMismatch = PolicyRule.builder()
                .requiresPasteEvent(false)
                .build();
        assertFalse(riskEngine.matchesRule(pasteMismatch, result, evidence, "chatgpt.com"));
    }

    @Test
    @DisplayName("Score arithmetic: base + sum of matched rules without dampening")
    void scoreArithmetic_NormalConfidence() {
        BrowserActivity activity = BrowserActivity.builder().domain("chatgpt.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_INTERACTION) // base = 25
                .confidence(0.85) // >= 0.60
                .modelVersion("v1")
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .pasteEventCount(1)
                .fileUploadPresent(true)
                .build();

        PolicyRule rule1 = PolicyRule.builder()
                .ruleKey("RULE_PASTE")
                .description("Pasted into prompt")
                .requiresPasteEvent(true)
                .scoreWeight(15)
                .build();

        PolicyRule rule2 = PolicyRule.builder()
                .ruleKey("RULE_FILE")
                .description("Uploaded file")
                .requiresFileUpload(true)
                .scoreWeight(20)
                .build();

        CompanyPolicy policy = CompanyPolicy.builder()
                .name("Test Policy")
                .version(1)
                .rules(List.of(rule1, rule2))
                .build();

        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy);

        // base (25) + rule1 (15) + rule2 (20) = 60 -> HIGH (50 <= 60 < 75)
        assertEquals(60, eval.getRiskScore());
        assertEquals(RiskLevel.HIGH, eval.getRiskLevel());
        assertTrue(eval.getMatchedRuleIds().contains("RULE_PASTE"));
        assertTrue(eval.getMatchedRuleIds().contains("RULE_FILE"));
        assertTrue(eval.getReasoning().contains("Base score: 25"));
        assertTrue(eval.getReasoning().contains("Final risk level: HIGH"));
    }

    @Test
    @DisplayName("Low confidence dampening (< 0.60) multiplies raw score by 0.7")
    void scoreArithmetic_LowConfidenceDampened() {
        BrowserActivity activity = BrowserActivity.builder().domain("unknown.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_GENERATION) // base = 40
                .confidence(0.50) // < 0.60 -> dampening triggered!
                .modelVersion("v1")
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .fileUploadPresent(true)
                .build();

        PolicyRule rule = PolicyRule.builder()
                .ruleKey("RULE_FILE")
                .description("Uploaded file")
                .requiresFileUpload(true)
                .scoreWeight(30)
                .build();

        CompanyPolicy policy = CompanyPolicy.builder()
                .name("Test Policy")
                .version(1)
                .rules(List.of(rule))
                .build();

        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy);

        // raw score = 40 + 30 = 70
        // confidence = 0.50 (< 0.60) -> 70 * 0.7 = 49 -> MEDIUM (25 <= 49 < 50)
        assertEquals(49, eval.getRiskScore());
        assertEquals(RiskLevel.MEDIUM, eval.getRiskLevel());
        assertTrue(eval.getReasoning().contains("low-confidence dampening"));
    }

    @Test
    @DisplayName("Score clamping: score capped at 100 even if rule sum exceeds 100")
    void scoreClamping_CappedAt100() {
        BrowserActivity activity = BrowserActivity.builder().domain("chatgpt.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_GENERATION) // base = 40
                .confidence(0.99)
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .fileUploadPresent(true)
                .pasteEventCount(5)
                .build();

        PolicyRule rule1 = PolicyRule.builder().ruleKey("R1").description("R1").requiresFileUpload(true).scoreWeight(40).build();
        PolicyRule rule2 = PolicyRule.builder().ruleKey("R2").description("R2").requiresPasteEvent(true).scoreWeight(40).build();

        CompanyPolicy policy = CompanyPolicy.builder()
                .rules(List.of(rule1, rule2))
                .build();

        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy);

        // raw score = 40 + 40 + 40 = 120 -> clamped to 100 -> CRITICAL
        assertEquals(100, eval.getRiskScore());
        assertEquals(RiskLevel.CRITICAL, eval.getRiskLevel());
        assertTrue(eval.getReasoning().contains("clamped to 100"));
    }

    @Test
    @DisplayName("Reasoning string is generated and safely capped within 1000 characters")
    void reasoning_WithinMax1000Chars() {
        BrowserActivity activity = BrowserActivity.builder().domain("chatgpt.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.NON_AI)
                .confidence(0.90)
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder().activity(activity).build();
        CompanyPolicy policy = CompanyPolicy.builder().rules(List.of()).build();

        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy);

        assertNotNull(eval.getReasoning());
        assertTrue(eval.getReasoning().length() <= 1000);
        assertTrue(eval.getReasoning().contains("Detected class: NON_AI"));
        assertTrue(eval.getReasoning().contains("Base score: 0"));
        assertTrue(eval.getReasoning().contains("Matched rules: None"));
        assertEquals("NONE", eval.getMatchedRuleIds());
        assertEquals(0, eval.getRiskScore());
        assertEquals(RiskLevel.LOW, eval.getRiskLevel());
    }
}
