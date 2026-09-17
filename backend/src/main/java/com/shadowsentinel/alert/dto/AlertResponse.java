package com.shadowsentinel.alert.dto;

import com.shadowsentinel.alert.AlertStatus;
import com.shadowsentinel.risk.Severity;

import java.time.Instant;

public class AlertResponse {

    private Long id;
    private Long userId;
    private Long activityId;
    private String domain;
    private Long riskAssessmentId;
    private Severity severity;
    private String title;
    private String message;
    private AlertStatus status;
    private int occurrenceCount;
    private Instant createdAt;
    private Instant acknowledgedAt;

    public AlertResponse() {
    }

    public AlertResponse(Long id, Long userId, Long activityId, String domain, Long riskAssessmentId,
                         Severity severity, String title, String message, AlertStatus status,
                         int occurrenceCount, Instant createdAt, Instant acknowledgedAt) {
        this.id = id;
        this.userId = userId;
        this.activityId = activityId;
        this.domain = domain;
        this.riskAssessmentId = riskAssessmentId;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.status = status;
        this.occurrenceCount = occurrenceCount;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private Long activityId;
        private String domain;
        private Long riskAssessmentId;
        private Severity severity;
        private String title;
        private String message;
        private AlertStatus status;
        private int occurrenceCount;
        private Instant createdAt;
        private Instant acknowledgedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder activityId(Long activityId) {
            this.activityId = activityId;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder riskAssessmentId(Long riskAssessmentId) {
            this.riskAssessmentId = riskAssessmentId;
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

        public AlertResponse build() {
            return new AlertResponse(id, userId, activityId, domain, riskAssessmentId,
                    severity, title, message, status, occurrenceCount, createdAt, acknowledgedAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getRiskAssessmentId() {
        return riskAssessmentId;
    }

    public void setRiskAssessmentId(Long riskAssessmentId) {
        this.riskAssessmentId = riskAssessmentId;
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
}
