package com.shadowsentinel.risk.dto;

import com.shadowsentinel.risk.RiskLevel;

import java.time.Instant;

public class RiskAssessmentResponse {

    private Long id;
    private Long activityId;
    private int riskScore;
    private RiskLevel riskLevel;
    private String matchedRuleIds;
    private String reasoning;
    private int policyVersion;
    private String modelVersion;
    private Instant createdAt;

    public RiskAssessmentResponse() {
    }

    public RiskAssessmentResponse(Long id, Long activityId, int riskScore, RiskLevel riskLevel,
                                  String matchedRuleIds, String reasoning, int policyVersion,
                                  String modelVersion, Instant createdAt) {
        this.id = id;
        this.activityId = activityId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.matchedRuleIds = matchedRuleIds;
        this.reasoning = reasoning;
        this.policyVersion = policyVersion;
        this.modelVersion = modelVersion;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long activityId;
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

        public Builder activityId(Long activityId) {
            this.activityId = activityId;
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

        public RiskAssessmentResponse build() {
            return new RiskAssessmentResponse(id, activityId, riskScore, riskLevel,
                    matchedRuleIds, reasoning, policyVersion, modelVersion, createdAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
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
}
