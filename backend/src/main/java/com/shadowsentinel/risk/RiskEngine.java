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
        String domain = evidence.getActivity() != null ? evidence.getActivity().getDomain() : "";
        List<PolicyRule> activeRules = policy != null && policy.getRules() != null ? policy.getRules() : List.of();

        List<PolicyRule> matchedRules = new ArrayList<>();
        for (PolicyRule rule : activeRules) {
            if (matchesRule(rule, result, evidence, domain)) {
                matchedRules.add(rule);
            }
        }

        int baseScore = getBaseScore(result.getClassLabel());
        int ruleWeightSum = matchedRules.stream().mapToInt(PolicyRule::getScoreWeight).sum();
        int rawScore = baseScore + ruleWeightSum;

        boolean isLowConfidence = result.getConfidence() < 0.60;
        int adjustedScore;
        if (isLowConfidence) {
            adjustedScore = (int) Math.round(rawScore * 0.7);
        } else {
            adjustedScore = rawScore;
        }

        int finalScore = Math.min(100, Math.max(0, adjustedScore));
        RiskLevel level = determineRiskLevel(finalScore);

        String matchedRuleIds = matchedRules.isEmpty() ? "NONE" :
                matchedRules.stream().map(r -> r.getRuleKey() != null ? r.getRuleKey() : String.valueOf(r.getId()))
                        .collect(Collectors.joining(","));

        String reasoning = generateReasoning(result, baseScore, matchedRules, rawScore, isLowConfidence, adjustedScore, finalScore, level);

        int policyVer = policy != null ? policy.getVersion() : 1;
        String modelVer = result.getModelVersion() != null ? result.getModelVersion() : "v1";

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

    private String generateReasoning(ClassificationResult result, int baseScore, List<PolicyRule> matchedRules,
                                     int rawScore, boolean isLowConfidence, int adjustedScore, int finalScore, RiskLevel level) {
        StringBuilder sb = new StringBuilder();
        int confidencePct = (int) Math.round(result.getConfidence() * 100);
        sb.append("Detected class: ").append(result.getClassLabel())
                .append(" (confidence: ").append(confidencePct).append("%). ");
        sb.append("Base score: ").append(baseScore).append(". ");

        if (matchedRules.isEmpty()) {
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

        int ruleSum = matchedRules.stream().mapToInt(PolicyRule::getScoreWeight).sum();
        if (isLowConfidence) {
            sb.append("Score calculation: (Base (").append(baseScore).append(") + Rules (")
                    .append(ruleSum).append(") = ").append(rawScore)
                    .append(") * 0.7 (low-confidence dampening) = ").append(adjustedScore);
        } else {
            sb.append("Score calculation: Base (").append(baseScore).append(") + Rules (")
                    .append(ruleSum).append(") = ").append(rawScore);
        }

        if (rawScore > 100 || adjustedScore > 100) {
            sb.append(" -> clamped to 100");
        }
        sb.append(". Final risk level: ").append(level).append(".");

        String reasoning = sb.toString();
        if (reasoning.length() > 1000) {
            return reasoning.substring(0, 997) + "...";
        }
        return reasoning;
    }
}
