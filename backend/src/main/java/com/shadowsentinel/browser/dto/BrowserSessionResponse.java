package com.shadowsentinel.browser.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shadowsentinel.browser.SessionStatus;

import java.time.Instant;

public class BrowserSessionResponse {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant endedAt;

    private SessionStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    public BrowserSessionResponse() {
    }

    public BrowserSessionResponse(Long id, Instant startedAt, Instant endedAt, SessionStatus status, Instant createdAt) {
        this.id = id;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Instant startedAt;
        private Instant endedAt;
        private SessionStatus status;
        private Instant createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder endedAt(Instant endedAt) {
            this.endedAt = endedAt;
            return this;
        }

        public Builder status(SessionStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BrowserSessionResponse build() {
            return new BrowserSessionResponse(id, startedAt, endedAt, status, createdAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
