package com.shadowsentinel.risk.dto;

import com.shadowsentinel.classification.ClassLabel;
import com.shadowsentinel.risk.Severity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePolicyRuleRequest {

    private Long policyId;

    @NotBlank(message = "ruleKey is required")
    private String ruleKey;

    private ClassLabel appliesToLabel;

    private String domainPattern;

    private Integer minGenerateClicks;

    private Boolean requiresFileUpload;

    private Boolean requiresPasteEvent;

    @NotNull(message = "severity is required")
    private Severity severity;

    @Min(value = 0, message = "scoreWeight must be between 0 and 40")
    @Max(value = 40, message = "scoreWeight must be between 0 and 40")
    private int scoreWeight;

    @NotBlank(message = "description is required")
    private String description;

    public CreatePolicyRuleRequest() {
    }

    public CreatePolicyRuleRequest(Long policyId, String ruleKey, ClassLabel appliesToLabel,
                                   String domainPattern, Integer minGenerateClicks, Boolean requiresFileUpload,
                                   Boolean requiresPasteEvent, Severity severity, int scoreWeight, String description) {
        this.policyId = policyId;
        this.ruleKey = ruleKey;
        this.appliesToLabel = appliesToLabel;
        this.domainPattern = domainPattern;
        this.minGenerateClicks = minGenerateClicks;
        this.requiresFileUpload = requiresFileUpload;
        this.requiresPasteEvent = requiresPasteEvent;
        this.severity = severity;
        this.scoreWeight = scoreWeight;
        this.description = description;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long policyId;
        private String ruleKey;
        private ClassLabel appliesToLabel;
        private String domainPattern;
        private Integer minGenerateClicks;
        private Boolean requiresFileUpload;
        private Boolean requiresPasteEvent;
        private Severity severity;
        private int scoreWeight;
        private String description;

        public Builder policyId(Long policyId) {
            this.policyId = policyId;
            return this;
        }

        public Builder ruleKey(String ruleKey) {
            this.ruleKey = ruleKey;
            return this;
        }

        public Builder appliesToLabel(ClassLabel appliesToLabel) {
            this.appliesToLabel = appliesToLabel;
            return this;
        }

        public Builder domainPattern(String domainPattern) {
            this.domainPattern = domainPattern;
            return this;
        }

        public Builder minGenerateClicks(Integer minGenerateClicks) {
            this.minGenerateClicks = minGenerateClicks;
            return this;
        }

        public Builder requiresFileUpload(Boolean requiresFileUpload) {
            this.requiresFileUpload = requiresFileUpload;
            return this;
        }

        public Builder requiresPasteEvent(Boolean requiresPasteEvent) {
            this.requiresPasteEvent = requiresPasteEvent;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder scoreWeight(int scoreWeight) {
            this.scoreWeight = scoreWeight;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public CreatePolicyRuleRequest build() {
            return new CreatePolicyRuleRequest(policyId, ruleKey, appliesToLabel, domainPattern,
                    minGenerateClicks, requiresFileUpload, requiresPasteEvent, severity, scoreWeight, description);
        }
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

    public ClassLabel getAppliesToLabel() {
        return appliesToLabel;
    }

    public void setAppliesToLabel(ClassLabel appliesToLabel) {
        this.appliesToLabel = appliesToLabel;
    }

    public String getDomainPattern() {
        return domainPattern;
    }

    public void setDomainPattern(String domainPattern) {
        this.domainPattern = domainPattern;
    }

    public Integer getMinGenerateClicks() {
        return minGenerateClicks;
    }

    public void setMinGenerateClicks(Integer minGenerateClicks) {
        this.minGenerateClicks = minGenerateClicks;
    }

    public Boolean getRequiresFileUpload() {
        return requiresFileUpload;
    }

    public void setRequiresFileUpload(Boolean requiresFileUpload) {
        this.requiresFileUpload = requiresFileUpload;
    }

    public Boolean getRequiresPasteEvent() {
        return requiresPasteEvent;
    }

    public void setRequiresPasteEvent(Boolean requiresPasteEvent) {
        this.requiresPasteEvent = requiresPasteEvent;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public int getScoreWeight() {
        return scoreWeight;
    }

    public void setScoreWeight(int scoreWeight) {
        this.scoreWeight = scoreWeight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
