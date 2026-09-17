package com.shadowsentinel.browser;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "browser_activities",
    indexes = {
        @Index(name = "idx_browser_activities_session_id", columnList = "session_id"),
        @Index(name = "idx_browser_activities_domain", columnList = "domain")
    }
)
public class BrowserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private BrowserSession session;

    @Column(nullable = false)
    private String domain;

    @Column(length = 500)
    private String url;

    @Column(name = "page_title")
    private String pageTitle;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BrowserActivity() {
    }

    public BrowserActivity(Long id, BrowserSession session, String domain, String url, String pageTitle,
                           Instant startedAt, Instant endedAt, Integer durationSeconds, Instant createdAt) {
        this.id = id;
        this.session = session;
        this.domain = domain;
        this.url = url;
        this.pageTitle = pageTitle;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private BrowserSession session;
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

        public Builder session(BrowserSession session) {
            this.session = session;
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

        public BrowserActivity build() {
            return new BrowserActivity(id, session, domain, url, pageTitle, startedAt, endedAt, durationSeconds, createdAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BrowserSession getSession() {
        return session;
    }

    public void setSession(BrowserSession session) {
        this.session = session;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrowserActivity that = (BrowserActivity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
