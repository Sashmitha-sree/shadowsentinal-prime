package com.shadowsentinel.risk.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CompanyPolicyResponse {

    private Long id;
    private Long companyId;
    private String name;
    private boolean active;
    private int version;
    private Instant createdAt;
    private List<PolicyRuleResponse> rules = new ArrayList<>();

    public CompanyPolicyResponse() {
    }

    public CompanyPolicyResponse(Long id, Long companyId, String name, boolean active, int version,
                                 Instant createdAt, List<PolicyRuleResponse> rules) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.active = active;
        this.version = version;
        this.createdAt = createdAt;
        if (rules != null) {
            this.rules = rules;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long companyId;
        private String name;
        private boolean active;
        private int version;
        private Instant createdAt;
        private List<PolicyRuleResponse> rules = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder companyId(Long companyId) {
            this.companyId = companyId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder rules(List<PolicyRuleResponse> rules) {
            this.rules = rules != null ? rules : new ArrayList<>();
            return this;
        }

        public CompanyPolicyResponse build() {
            return new CompanyPolicyResponse(id, companyId, name, active, version, createdAt, rules);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<PolicyRuleResponse> getRules() {
        return rules;
    }

    public void setRules(List<PolicyRuleResponse> rules) {
        this.rules = rules;
    }
}
