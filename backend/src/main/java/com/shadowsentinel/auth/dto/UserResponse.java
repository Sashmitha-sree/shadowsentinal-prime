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

    public UserResponse() {
    }

    public UserResponse(Long id, String email, Role role, Long companyId, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.companyId = companyId;
        this.createdAt = createdAt;
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

        public UserResponse build() {
            return new UserResponse(id, email, role, companyId, createdAt);
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
}
