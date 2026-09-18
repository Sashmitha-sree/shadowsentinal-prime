package com.shadowsentinel.risk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shadowsentinel.risk.AiDomainStatus;

import java.time.Instant;

public class AiDomainResponse {

    private Long id;
    private String domain;
    private AiDomainStatus status;
    private String addedBy;
    private String notes;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant firstSeenAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;

    public AiDomainResponse() {
    }

    public AiDomainResponse(Long id, String domain, AiDomainStatus status, String addedBy, String notes, Instant firstSeenAt, Instant updatedAt) {
        this.id = id;
        this.domain = domain;
        this.status = status;
        this.addedBy = addedBy;
        this.notes = notes;
        this.firstSeenAt = firstSeenAt;
        this.updatedAt = updatedAt;
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

        public AiDomainResponse build() {
            return new AiDomainResponse(id, domain, status, addedBy, notes, firstSeenAt, updatedAt);
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
}
