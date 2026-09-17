package com.shadowsentinel.risk;

import com.shadowsentinel.classification.ClassLabel;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "policy_rules")
public class PolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private CompanyPolicy policy;

    @Column(name = "rule_key", nullable = false)
    private String ruleKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to_label")
    private ClassLabel appliesToLabel;

    @Column(name = "domain_pattern")
    private String domainPattern;

    @Column(name = "min_generate_clicks")
    private Integer minGenerateClicks;

    @Column(name = "requires_file_upload")
    private Boolean requiresFileUpload;

    @Column(name = "requires_paste_event")
    private Boolean requiresPasteEvent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity;

    @Column(name = "score_weight", nullable = false)
    private int scoreWeight = 0;

    @Column(nullable = false)
    private String description;

    public PolicyRule() {
    }

    public PolicyRule(Long id, CompanyPolicy policy, String ruleKey, ClassLabel appliesToLabel,
                      String domainPattern, Integer minGenerateClicks, Boolean requiresFileUpload,
                      Boolean requiresPasteEvent, Severity severity, int scoreWeight, String description) {
        this.id = id;
        this.policy = policy;
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
        private Long id;
        private CompanyPolicy policy;
        private String ruleKey;
        private ClassLabel appliesToLabel;
        private String domainPattern;
        private Integer minGenerateClicks;
        private Boolean requiresFileUpload;
        private Boolean requiresPasteEvent;
        private Severity severity;
        private int scoreWeight = 0;
        private String description;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder policy(CompanyPolicy policy) {
            this.policy = policy;
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

        public PolicyRule build() {
            return new PolicyRule(id, policy, ruleKey, appliesToLabel, domainPattern,
                    minGenerateClicks, requiresFileUpload, requiresPasteEvent, severity, scoreWeight, description);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CompanyPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(CompanyPolicy policy) {
        this.policy = policy;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyRule that = (PolicyRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
