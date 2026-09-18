package com.shadowsentinel.risk;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "ai_domains",
    indexes = {
        @Index(name = "idx_ai_domains_domain", columnList = "domain", unique = true),
        @Index(name = "idx_ai_domains_status", columnList = "status")
    }
)
public class AiDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AiDomainStatus status;

    @Column(name = "added_by")
    private String addedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public AiDomain() {
    }

    public AiDomain(Long id, String domain, AiDomainStatus status, String addedBy, String notes, Instant firstSeenAt, Instant updatedAt) {
        this.id = id;
        this.domain = domain;
        this.status = status;
        this.addedBy = addedBy;
        this.notes = notes;
        this.firstSeenAt = firstSeenAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.firstSeenAt == null) {
            this.firstSeenAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String domain;
        private AiDomainStatus status;
        private String addedBy;
        private String notes;
        private Instant firstSeenAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder status(AiDomainStatus status) {
            this.status = status;
            return this;
        }

        public Builder addedBy(String addedBy) {
            this.addedBy = addedBy;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder firstSeenAt(Instant firstSeenAt) {
            this.firstSeenAt = firstSeenAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AiDomain build() {
            return new AiDomain(id, domain, status, addedBy, notes, firstSeenAt, updatedAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public AiDomainStatus getStatus() {
        return status;
    }

    public void setStatus(AiDomainStatus status) {
        this.status = status;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiDomain aiDomain = (AiDomain) o;
        return Objects.equals(id, aiDomain.id) && Objects.equals(domain, aiDomain.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domain);
    }
}
