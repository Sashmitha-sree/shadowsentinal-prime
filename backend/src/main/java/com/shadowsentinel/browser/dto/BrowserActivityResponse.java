package com.shadowsentinel.browser.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class BrowserActivityResponse {

    private Long id;
    private Long sessionId;
    private String domain;
    private String url;
    private String pageTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant endedAt;

    private Integer durationSeconds;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    public BrowserActivityResponse() {
    }

    public BrowserActivityResponse(Long id, Long sessionId, String domain, String url, String pageTitle,
                                   Instant startedAt, Instant endedAt, Integer durationSeconds, Instant createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.domain = domain;
        this.url = url;
        this.pageTitle = pageTitle;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long sessionId;
        private String domain;
        private String url;
        private String pageTitle;
        private Instant startedAt;
        private Instant endedAt;
        private Integer durationSeconds;
        private Instant createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BrowserActivityResponse build() {
            return new BrowserActivityResponse(id, sessionId, domain, url, pageTitle, startedAt, endedAt, durationSeconds, createdAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
