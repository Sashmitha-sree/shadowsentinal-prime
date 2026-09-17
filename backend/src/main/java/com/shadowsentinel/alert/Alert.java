package com.shadowsentinel.alert;

import com.shadowsentinel.auth.User;
import com.shadowsentinel.browser.BrowserActivity;
import com.shadowsentinel.risk.RiskAssessment;
import com.shadowsentinel.risk.Severity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private BrowserActivity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "risk_assessment_id", nullable = false)
    private RiskAssessment riskAssessment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertStatus status = AlertStatus.NEW;

    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    public Alert() {
    }

    public Alert(Long id, User user, BrowserActivity activity, RiskAssessment riskAssessment,
                 Severity severity, String title, String message, AlertStatus status,
                 int occurrenceCount, Instant createdAt, Instant acknowledgedAt) {
        this.id = id;
        this.user = user;
        this.activity = activity;
        this.riskAssessment = riskAssessment;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.status = status != null ? status : AlertStatus.NEW;
        this.occurrenceCount = occurrenceCount > 0 ? occurrenceCount : 1;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = AlertStatus.NEW;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private BrowserActivity activity;
        private RiskAssessment riskAssessment;
        private Severity severity;
        private String title;
        private String message;
        private AlertStatus status = AlertStatus.NEW;
        private int occurrenceCount = 1;
        private Instant createdAt;
        private Instant acknowledgedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder activity(BrowserActivity activity) {
            this.activity = activity;
            return this;
        }

        public Builder riskAssessment(RiskAssessment riskAssessment) {
            this.riskAssessment = riskAssessment;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(AlertStatus status) {
            this.status = status;
            return this;
        }

        public Builder occurrenceCount(int occurrenceCount) {
            this.occurrenceCount = occurrenceCount;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder acknowledgedAt(Instant acknowledgedAt) {
            this.acknowledgedAt = acknowledgedAt;
            return this;
        }

        public Alert build() {
            return new Alert(id, user, activity, riskAssessment, severity, title, message,
                    status, occurrenceCount, createdAt, acknowledgedAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BrowserActivity getActivity() {
        return activity;
    }

    public void setActivity(BrowserActivity activity) {
        this.activity = activity;
    }

    public RiskAssessment getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(RiskAssessment riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return Objects.equals(id, alert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
