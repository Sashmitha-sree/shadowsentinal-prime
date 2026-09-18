package com.shadowsentinel.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shadowsentinel.auth.Role;

import java.time.Instant;

public class UserResponse {

    private Long id;
    private String email;
    private Role role;
    private Long companyId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant lastSeenAt;

    private Boolean online;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, Role role, Long companyId, Instant createdAt) {
        this(id, email, role, companyId, createdAt, null, null);
    }

    public UserResponse(Long id, String email, Role role, Long companyId, Instant createdAt, Instant lastSeenAt, Boolean online) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.companyId = companyId;
        this.createdAt = createdAt;
        this.lastSeenAt = lastSeenAt;
        this.online = online;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private Role role;
        private Long companyId;
        private Instant createdAt;
        private Instant lastSeenAt;
        private Boolean online;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder companyId(Long companyId) {
            this.companyId = companyId;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastSeenAt(Instant lastSeenAt) {
            this.lastSeenAt = lastSeenAt;
            return this;
        }

        public Builder online(Boolean online) {
            this.online = online;
            return this;
        }

        public UserResponse build() {
            return new UserResponse(id, email, role, companyId, createdAt, lastSeenAt, online);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Boolean getOnline() {
        return online;
    }

    public Boolean isOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
