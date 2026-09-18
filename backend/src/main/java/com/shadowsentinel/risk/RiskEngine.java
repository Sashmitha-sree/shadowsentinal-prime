package com.shadowsentinel.risk;

import com.shadowsentinel.classification.ClassLabel;
import com.shadowsentinel.classification.ClassificationEvidence;
import com.shadowsentinel.classification.ClassificationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class RiskEngine {

    public static class Evaluation {
        private final int riskScore;
        private final RiskLevel riskLevel;
        private final String matchedRuleIds;
        private final String reasoning;
        private final int policyVersion;
        private final String modelVersion;

        public Evaluation(int riskScore, RiskLevel riskLevel, String matchedRuleIds,
                          String reasoning, int policyVersion, String modelVersion) {
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
            this.matchedRuleIds = matchedRuleIds;
            this.reasoning = reasoning;
            this.policyVersion = policyVersion;
            this.modelVersion = modelVersion;
        }

        public int getRiskScore() {
            return riskScore;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public String getMatchedRuleIds() {
            return matchedRuleIds;
        }

        public String getReasoning() {
            return reasoning;
        }

        public int getPolicyVersion() {
            return policyVersion;
        }

        public String getModelVersion() {
            return modelVersion;
        }
    }

    public Evaluation evaluate(ClassificationEvidence evidence, ClassificationResult result, CompanyPolicy policy) {
        return evaluate(evidence, result, policy, AiDomainStatus.UNKNOWN);
    }

    public Evaluation evaluate(ClassificationEvidence evidence, ClassificationResult result, CompanyPolicy policy, AiDomainStatus domainStatus) {
        if (domainStatus == null) {
            domainStatus = AiDomainStatus.UNKNOWN;
        }

        String domain = (evidence != null && evidence.getActivity() != null) ? evidence.getActivity().getDomain() : "";
        List<PolicyRule> activeRules = (policy != null && policy.getRules() != null) ? policy.getRules() : List.of();

        List<PolicyRule> matchedRules = new ArrayList<>();
        for (PolicyRule rule : activeRules) {
            if (matchesRule(rule, result, evidence, domain)) {
                matchedRules.add(rule);
            }
        }

        String matchedRuleIds = matchedRules.isEmpty() ? "NONE" :
                matchedRules.stream().map(r -> r.getRuleKey() != null ? r.getRuleKey() : String.valueOf(r.getId()))
                        .collect(Collectors.joining(","));

        ClassLabel label = result != null ? result.getClassLabel() : null;
        double confidence = result != null ? result.getConfidence() : 1.0;
        boolean isLowConfidence = confidence < 0.60;

        int finalScore = 0;
        RiskLevel level = RiskLevel.LOW;
        String statusLine = "";
        String scoreCalcText = "";

        // 1. If classification label is NON_AI:
        //    riskScore = 0, riskLevel = LOW, regardless of any other signal.
        if (label == null || label == ClassLabel.NON_AI) {
            finalScore = 0;
            level = RiskLevel.LOW;
            statusLine = "Activity classified as non-AI; zero risk assigned.";
            scoreCalcText = "Score calculation: Non-AI activity -> risk score set to 0.";
        } else {
            // 2. If classification label is AI-related:
            switch (domainStatus) {
                case BLOCKED -> {
                    // BLOCKED: riskLevel = CRITICAL, riskScore = 100, regardless of interaction depth or confidence
                    finalScore = 100;
                    level = RiskLevel.CRITICAL;
                    statusLine = "Domain is on the blocked AI list set by admin.";
                    scoreCalcText = "Score calculation: Blocked AI domain -> risk score set to 100.";
                }
                case APPROVED -> {
                    // APPROVED: base score at 25% weight, rules apply, confidence dampening applies, capped at 49
                    statusLine = "Domain is approved for AI use; risk capped accordingly.";
                    int baseScore = getBaseScore(label);
                    int effectiveBase = (int) Math.round(baseScore * 0.25);
                    int ruleWeightSum = matchedRules.stream().mapToInt(PolicyRule::getScoreWeight).sum();
                    int rawScore = effectiveBase + ruleWeightSum;

                    int adjustedScore;
                    if (isLowConfidence) {
                        adjustedScore = (int) Math.round(rawScore * 0.7);
                        scoreCalcText = String.format("Score calculation: (Base (%d) + Rules (%d) = %d) * 0.7 (low-confidence dampening) = %d",
                                effectiveBase, ruleWeightSum, rawScore, adjustedScore);
                    } else {
                        adjustedScore = rawScore;
                        scoreCalcText = String.format("Score calculation: Base (%d) + Rules (%d) = %d",
                                effectiveBase, ruleWeightSum, rawScore);
                    }

                    if (adjustedScore > 49) {
                        scoreCalcText += " -> capped at 49 (approved domain ceiling)";
                    }
                    scoreCalcText += ".";

                    finalScore = Math.min(49, Math.max(0, adjustedScore));
                    level = determineRiskLevel(finalScore);
                }
                case UNKNOWN -> {
                    // UNKNOWN: base + rules + 15, confidence dampening applies, clamped to 100
                    statusLine = "Domain has not been classified by admin (unknown AI service); risk score elevated pending review.";
                    int baseScore = getBaseScore(label);
                    int ruleWeightSum = matchedRules.stream().mapToInt(PolicyRule::getScoreWeight).sum();
                    int rawScore = baseScore + ruleWeightSum + 15;

                    int adjustedScore;
                    if (isLowConfidence) {
                        adjustedScore = (int) Math.round(rawScore * 0.7);
                        scoreCalcText = String.format("Score calculation: (Base (%d) + Rules (%d) + Elevation (15) = %d) * 0.7 (low-confidence dampening) = %d",
                                baseScore, ruleWeightSum, rawScore, adjustedScore);
                    } else {
                        adjustedScore = rawScore;
                        scoreCalcText = String.format("Score calculation: Base (%d) + Rules (%d) + Elevation (15) = %d",
                                baseScore, ruleWeightSum, rawScore);
                    }

                    if (adjustedScore > 100) {
                        scoreCalcText += " -> clamped to 100";
                    }
                    scoreCalcText += ".";

                    finalScore = Math.min(100, Math.max(0, adjustedScore));
                    level = determineRiskLevel(finalScore);
                }
            }
        }

        String reasoning = generateReasoning(result, statusLine, matchedRules, scoreCalcText, level, domainStatus);

        int policyVer = policy != null ? policy.getVersion() : 1;
        String modelVer = (result != null && result.getModelVersion() != null) ? result.getModelVersion() : "v1";

        return new Evaluation(finalScore, level, matchedRuleIds, reasoning, policyVer, modelVer);
    }

    public int getBaseScore(ClassLabel label) {
        if (label == null) {
            return 0;
        }
        return switch (label) {
            case NON_AI -> 0;
            case AI_CAPABLE_PAGE -> 10;
            case AI_INTERACTION -> 25;
            case AI_GENERATION -> 40;
        };
    }

    public RiskLevel determineRiskLevel(int score) {
        if (score < 25) {
            return RiskLevel.LOW;
        } else if (score < 50) {
            return RiskLevel.MEDIUM;
        } else if (score < 75) {
            return RiskLevel.HIGH;
        } else {
            return RiskLevel.CRITICAL;
        }
    }

    public boolean matchesRule(PolicyRule rule, ClassificationResult result, ClassificationEvidence evidence, String domain) {
        // 1. appliesToLabel
        if (rule.getAppliesToLabel() != null && rule.getAppliesToLabel() != result.getClassLabel()) {
            return false;
        }

        // 2. domainPattern
        if (rule.getDomainPattern() != null && !rule.getDomainPattern().isBlank()) {
            if (!matchesGlob(domain != null ? domain : "", rule.getDomainPattern())) {
                return false;
            }
        }

        // 3. minGenerateClicks
        if (rule.getMinGenerateClicks() != null) {
            if (evidence.getGenerateClickCount() < rule.getMinGenerateClicks()) {
                return false;
            }
        }

        // 4. requiresFileUpload
        if (rule.getRequiresFileUpload() != null) {
            if (evidence.isFileUploadPresent() != rule.getRequiresFileUpload()) {
                return false;
            }
        }

        // 5. requiresPasteEvent
        if (rule.getRequiresPasteEvent() != null) {
            boolean hasPaste = evidence.getPasteEventCount() > 0;
            if (hasPaste != rule.getRequiresPasteEvent()) {
                return false;
            }
        }

        return true;
    }

    public boolean matchesGlob(String text, String glob) {
        if (glob == null || text == null) {
            return false;
        }
        if ("*".equals(glob.trim())) {
            return true;
        }

        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> regex.append(".*");
                case '?' -> regex.append(".");
                case '.', '(', ')', '[', ']', '{', '}', '^', '$', '+', '|', '\\' -> regex.append("\\").append(c);
                default -> regex.append(c);
            }
        }
        regex.append("$");

        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE).matcher(text).matches();
    }

    private String generateReasoning(ClassificationResult result, String statusLine, List<PolicyRule> matchedRules,
                                     String scoreCalcText, RiskLevel level, AiDomainStatus domainStatus) {
        StringBuilder sb = new StringBuilder();
        sb.append(statusLine).append("\n");

        ClassLabel label = result != null ? result.getClassLabel() : null;
        double confidence = result != null ? result.getConfidence() : 1.0;
        int confidencePct = (int) Math.round(confidence * 100);
        sb.append("Detected class: ").append(label)
                .append(" (confidence: ").append(confidencePct).append("%). ");

        int baseScore = getBaseScore(label);
        if (domainStatus == AiDomainStatus.APPROVED && label != ClassLabel.NON_AI) {
            int reducedBase = (int) Math.round(baseScore * 0.25);
            sb.append("Base score: ").append(reducedBase).append(". ");
        } else {
            sb.append("Base score: ").append(baseScore).append(". ");
        }

        if (matchedRules == null || matchedRules.isEmpty()) {
            sb.append("Matched rules: None. ");
        } else {
            sb.append("Matched rules: ");
            for (int i = 0; i < matchedRules.size(); i++) {
                PolicyRule r = matchedRules.get(i);
                sb.append(r.getDescription())
                        .append(" (+").append(r.getScoreWeight()).append(")");
                if (i < matchedRules.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(". ");
        }

        sb.append(scoreCalcText).append(" Final risk level: ").append(level).append(".");

        String reasoning = sb.toString();
        if (reasoning.length() > 1000) {
            return reasoning.substring(0, 997) + "...";
        }
        return reasoning;
    }
}
