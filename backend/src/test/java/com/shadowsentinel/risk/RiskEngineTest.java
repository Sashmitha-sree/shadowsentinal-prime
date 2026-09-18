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
    @DisplayName("NON_AI never exceeds LOW (score = 0) regardless of domain status, interaction depth, or rules")
    void nonAi_NeverExceedsLow_RegardlessOfEvidenceAndRules() {
        BrowserActivity activity = BrowserActivity.builder().domain("blocked-ai.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.NON_AI)
                .confidence(0.99)
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .fileUploadPresent(true)
                .pasteEventCount(10)
                .generateClickCount(20)
                .build();

        PolicyRule heavyRule = PolicyRule.builder()
                .ruleKey("HEAVY_RULE")
                .description("Heavy violation rule")
                .scoreWeight(40)
                .build();

        CompanyPolicy policy = CompanyPolicy.builder()
                .rules(List.of(heavyRule))
                .build();

        // Even with BLOCKED status and matched rules, NON_AI must produce score 0 and LOW
        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy, AiDomainStatus.BLOCKED);

        assertEquals(0, eval.getRiskScore());
        assertEquals(RiskLevel.LOW, eval.getRiskLevel());
        assertTrue(eval.getReasoning().startsWith("Activity classified as non-AI; zero risk assigned."));
    }

    @Test
    @DisplayName("BLOCKED domain is always CRITICAL (score = 100) even with weak evidence and low confidence")
    void blockedDomain_AlwaysCritical_EvenWithWeakEvidenceAndLowConfidence() {
        BrowserActivity activity = BrowserActivity.builder().domain("banned-ai.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_CAPABLE_PAGE) // minimal base score
                .confidence(0.35) // low confidence (< 0.60)
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .build();

        CompanyPolicy policy = CompanyPolicy.builder()
                .rules(List.of())
                .build();

        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy, AiDomainStatus.BLOCKED);

        assertEquals(100, eval.getRiskScore(), "Blocked domain must always receive score 100");
        assertEquals(RiskLevel.CRITICAL, eval.getRiskLevel(), "Blocked domain must always be CRITICAL");
        assertTrue(eval.getReasoning().startsWith("Domain is on the blocked AI list set by admin."));
        assertFalse(eval.getReasoning().contains("low-confidence dampening"),
                "Blocked domain is exempt from confidence dampening");
    }

    @Test
    @DisplayName("APPROVED domain never exceeds MEDIUM (score capped at 49) even with maximal interaction evidence")
    void approvedDomain_NeverExceedsMedium_EvenWithMaximalInteractionEvidence() {
        BrowserActivity activity = BrowserActivity.builder().domain("corp-approved-ai.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_GENERATION) // base = 40 -> 25% weight = 10
                .confidence(0.99)
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .fileUploadPresent(true)
                .pasteEventCount(5)
                .generateClickCount(10)
                .build();

        PolicyRule rule1 = PolicyRule.builder().ruleKey("R1").description("R1").requiresFileUpload(true).scoreWeight(40).build();
        PolicyRule rule2 = PolicyRule.builder().ruleKey("R2").description("R2").requiresPasteEvent(true).scoreWeight(40).build();

        CompanyPolicy policy = CompanyPolicy.builder()
                .rules(List.of(rule1, rule2))
                .build();

        // Effective base (10) + rule1 (40) + rule2 (40) = 90 -> capped at 49
        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy, AiDomainStatus.APPROVED);

        assertEquals(49, eval.getRiskScore(), "Approved domain score must be capped at 49");
        assertEquals(RiskLevel.MEDIUM, eval.getRiskLevel(), "Approved domain must not exceed MEDIUM");
        assertTrue(eval.getReasoning().startsWith("Domain is approved for AI use; risk capped accordingly."));
        assertTrue(eval.getReasoning().contains("capped at 49 (approved domain ceiling)"));
    }

    @Test
    @DisplayName("APPROVED domain with low base score remains LOW when under 25")
    void approvedDomain_LowActivityStaysLow() {
        BrowserActivity activity = BrowserActivity.builder().domain("corp-approved-ai.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_CAPABLE_PAGE) // base = 10 -> 25% weight = 3
                .confidence(0.95)
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder().activity(activity).build();
        CompanyPolicy policy = CompanyPolicy.builder().rules(List.of()).build();

        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy, AiDomainStatus.APPROVED);

        assertEquals(3, eval.getRiskScore());
        assertEquals(RiskLevel.LOW, eval.getRiskLevel());
        assertTrue(eval.getReasoning().startsWith("Domain is approved for AI use; risk capped accordingly."));
    }

    @Test
    @DisplayName("UNKNOWN domain gets flat +15 elevation correctly")
    void unknownDomain_GetsFlat15ElevationCorrectly() {
        BrowserActivity activity = BrowserActivity.builder().domain("unreviewed-ai.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_INTERACTION) // base = 25
                .confidence(0.85) // >= 0.60
                .build();

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .activity(activity)
                .pasteEventCount(1)
                .build();

        PolicyRule rule = PolicyRule.builder()
                .ruleKey("RULE_PASTE")
                .description("Pasted prompt")
                .requiresPasteEvent(true)
                .scoreWeight(20)
                .build();

        CompanyPolicy policy = CompanyPolicy.builder()
                .rules(List.of(rule))
                .build();

        // Base (25) + Rule (20) + Elevation (15) = 60 -> HIGH
        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy, AiDomainStatus.UNKNOWN);

        assertEquals(60, eval.getRiskScore());
        assertEquals(RiskLevel.HIGH, eval.getRiskLevel());
        assertTrue(eval.getReasoning().startsWith("Domain has not been classified by admin (unknown AI service); risk score elevated pending review."));
        assertTrue(eval.getReasoning().contains("Elevation (15) = 60"));
    }

    @Test
    @DisplayName("UNKNOWN domain with low confidence (< 0.60) applies dampening after +15 elevation")
    void unknownDomain_LowConfidenceDampened() {
        BrowserActivity activity = BrowserActivity.builder().domain("unknown.com").build();
        ClassificationResult result = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_GENERATION) // base = 40
                .confidence(0.50) // < 0.60 -> dampening applied
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
                .rules(List.of(rule))
                .build();

        // Raw score = 40 + 30 + 15 = 85
        // Adjusted score = (int) Math.round(85 * 0.7) = 59 -> HIGH
        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy, AiDomainStatus.UNKNOWN);

        assertEquals(59, eval.getRiskScore());
        assertEquals(RiskLevel.HIGH, eval.getRiskLevel());
        assertTrue(eval.getReasoning().contains("low-confidence dampening"));
    }

    @Test
    @DisplayName("Score clamping: score capped at 100 even if rule sum and elevation exceed 100")
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

        // Raw score = 40 + 40 + 40 + 15 = 135 -> clamped to 100 -> CRITICAL
        RiskEngine.Evaluation eval = riskEngine.evaluate(evidence, result, policy, AiDomainStatus.UNKNOWN);

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

    @Test
    @DisplayName("Reasoning explicitly names domain status as the first line for all domain statuses")
    void reasoning_FirstLineExplicitlyNamesDomainStatus_AllBranches() {
        BrowserActivity activity = BrowserActivity.builder().domain("test.com").build();
        ClassificationResult aiResult = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.AI_INTERACTION)
                .confidence(0.95)
                .build();
        ClassificationEvidence evidence = ClassificationEvidence.builder().activity(activity).build();
        CompanyPolicy policy = CompanyPolicy.builder().rules(List.of()).build();

        // BLOCKED
        RiskEngine.Evaluation evalBlocked = riskEngine.evaluate(evidence, aiResult, policy, AiDomainStatus.BLOCKED);
        String[] linesBlocked = evalBlocked.getReasoning().split("\n");
        assertEquals("Domain is on the blocked AI list set by admin.", linesBlocked[0]);

        // APPROVED
        RiskEngine.Evaluation evalApproved = riskEngine.evaluate(evidence, aiResult, policy, AiDomainStatus.APPROVED);
        String[] linesApproved = evalApproved.getReasoning().split("\n");
        assertEquals("Domain is approved for AI use; risk capped accordingly.", linesApproved[0]);

        // UNKNOWN
        RiskEngine.Evaluation evalUnknown = riskEngine.evaluate(evidence, aiResult, policy, AiDomainStatus.UNKNOWN);
        String[] linesUnknown = evalUnknown.getReasoning().split("\n");
        assertEquals("Domain has not been classified by admin (unknown AI service); risk score elevated pending review.", linesUnknown[0]);

        // NON_AI
        ClassificationResult nonAiResult = ClassificationResult.builder()
                .activity(activity)
                .classLabel(ClassLabel.NON_AI)
                .confidence(0.95)
                .build();
        RiskEngine.Evaluation evalNonAi = riskEngine.evaluate(evidence, nonAiResult, policy, AiDomainStatus.UNKNOWN);
        String[] linesNonAi = evalNonAi.getReasoning().split("\n");
        assertEquals("Activity classified as non-AI; zero risk assigned.", linesNonAi[0]);
    }
}
