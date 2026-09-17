package com.shadowsentinel.browser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class CreateActivityRequest {

    @NotNull(message = "sessionId is required")
    private Long sessionId;

    @NotBlank(message = "domain is required")
    private String domain;

    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String url;

    private String pageTitle;

    @NotNull(message = "startedAt is required")
    private Instant startedAt;

    private Instant endedAt;

    private Integer durationSeconds;

    public CreateActivityRequest() {
    }

    public CreateActivityRequest(Long sessionId, String domain, String url, String pageTitle,
                                 Instant startedAt, Instant endedAt, Integer durationSeconds) {
        this.sessionId = sessionId;
        this.domain = domain;
        this.url = url;
        this.pageTitle = pageTitle;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long sessionId;
        private String domain;
        private String url;
        private String pageTitle;
        private Instant startedAt;
        private Instant endedAt;
        private Integer durationSeconds;

        public Builder sessionId(Long sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder pageTitle(String pageTitle) {
            this.pageTitle = pageTitle;
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

        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public CreateActivityRequest build() {
            return new CreateActivityRequest(sessionId, domain, url, pageTitle, startedAt, endedAt, durationSeconds);
        }
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
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

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}
