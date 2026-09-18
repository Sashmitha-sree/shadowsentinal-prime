package com.shadowsentinel.risk.dto;

import com.shadowsentinel.risk.AiDomainStatus;

public class UpdateAiDomainRequest {

    private AiDomainStatus status;
    private String notes;

    public UpdateAiDomainRequest() {
    }

    public UpdateAiDomainRequest(AiDomainStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AiDomainStatus status;
        private String notes;

        public Builder status(AiDomainStatus status) {
            this.status = status;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public UpdateAiDomainRequest build() {
            return new UpdateAiDomainRequest(status, notes);
        }
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
