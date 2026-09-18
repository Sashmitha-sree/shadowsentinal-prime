package com.shadowsentinel.risk.dto;

import com.shadowsentinel.risk.AiDomainStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAiDomainRequest {

    @NotBlank(message = "domain is required")
    private String domain;

    @NotNull(message = "status is required")
    private AiDomainStatus status;

    private String notes;

    public CreateAiDomainRequest() {
    }

    public CreateAiDomainRequest(String domain, AiDomainStatus status, String notes) {
        this.domain = domain;
        this.status = status;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String domain;
        private AiDomainStatus status;
        private String notes;

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder status(AiDomainStatus status) {
            this.status = status;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public CreateAiDomainRequest build() {
            return new CreateAiDomainRequest(domain, status, notes);
        }
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
