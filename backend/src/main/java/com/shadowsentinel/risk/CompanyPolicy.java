package com.shadowsentinel.risk;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "company_policies")
public class CompanyPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PolicyRule> rules = new ArrayList<>();

    public CompanyPolicy() {
    }

    public CompanyPolicy(Long id, Long companyId, String name, boolean active, int version, Instant createdAt, List<PolicyRule> rules) {
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

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public void addRule(PolicyRule rule) {
        rules.add(rule);
        rule.setPolicy(this);
    }

    public void removeRule(PolicyRule rule) {
        rules.remove(rule);
        rule.setPolicy(null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long companyId;
        private String name;
        private boolean active = true;
        private int version = 1;
        private Instant createdAt;
        private List<PolicyRule> rules = new ArrayList<>();

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

        public Builder rules(List<PolicyRule> rules) {
            this.rules = rules != null ? rules : new ArrayList<>();
            return this;
        }

        public CompanyPolicy build() {
            CompanyPolicy policy = new CompanyPolicy(id, companyId, name, active, version, createdAt, rules);
            for (PolicyRule rule : policy.getRules()) {
                rule.setPolicy(policy);
            }
            return policy;
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

    public List<PolicyRule> getRules() {
        return rules;
    }

    public void setRules(List<PolicyRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyPolicy that = (CompanyPolicy) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
