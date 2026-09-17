package com.shadowsentinel.audit.dto;

import com.shadowsentinel.audit.AuditEventType;

import java.time.Instant;

public class AuditLogResponse {

    private Long id;
    private Long userId;
    private AuditEventType eventType;
    private String targetId;
    private String details;
    private Instant createdAt;

    public AuditLogResponse() {
    }

    public AuditLogResponse(Long id, Long userId, AuditEventType eventType, String targetId, String details, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.eventType = eventType;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private AuditEventType eventType;
        private String targetId;
        private String details;
        private Instant createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder eventType(AuditEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AuditLogResponse build() {
            return new AuditLogResponse(id, userId, eventType, targetId, details, createdAt);
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

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
