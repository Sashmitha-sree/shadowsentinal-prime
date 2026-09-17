package com.shadowsentinel.risk;

import com.shadowsentinel.browser.BrowserActivity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private BrowserActivity activity;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 32)
    private RiskLevel riskLevel;

    @Column(name = "matched_rule_ids", nullable = false)
    private String matchedRuleIds;

    @Column(nullable = false, length = 1000)
    private String reasoning;

    @Column(name = "policy_version", nullable = false)
    private int policyVersion;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RiskAssessment() {
    }

    public RiskAssessment(Long id, BrowserActivity activity, int riskScore, RiskLevel riskLevel,
                          String matchedRuleIds, String reasoning, int policyVersion, String modelVersion, Instant createdAt) {
        this.id = id;
        this.activity = activity;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.matchedRuleIds = matchedRuleIds;
        this.reasoning = reasoning;
        this.policyVersion = policyVersion;
        this.modelVersion = modelVersion;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private BrowserActivity activity;
        private int riskScore;
        private RiskLevel riskLevel;
        private String matchedRuleIds;
        private String reasoning;
        private int policyVersion;
        private String modelVersion;
        private Instant createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder activity(BrowserActivity activity) {
            this.activity = activity;
            return this;
        }

        public Builder riskScore(int riskScore) {
            this.riskScore = riskScore;
            return this;
        }

        public Builder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public Builder matchedRuleIds(String matchedRuleIds) {
            this.matchedRuleIds = matchedRuleIds;
            return this;
        }

        public Builder reasoning(String reasoning) {
            this.reasoning = reasoning;
            return this;
        }

        public Builder policyVersion(int policyVersion) {
            this.policyVersion = policyVersion;
            return this;
        }

        public Builder modelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RiskAssessment build() {
            return new RiskAssessment(id, activity, riskScore, riskLevel, matchedRuleIds, reasoning, policyVersion, modelVersion, createdAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BrowserActivity getActivity() {
        return activity;
    }

    public void setActivity(BrowserActivity activity) {
        this.activity = activity;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getMatchedRuleIds() {
        return matchedRuleIds;
    }

    public void setMatchedRuleIds(String matchedRuleIds) {
        this.matchedRuleIds = matchedRuleIds;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public int getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(int policyVersion) {
        this.policyVersion = policyVersion;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiskAssessment that = (RiskAssessment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
