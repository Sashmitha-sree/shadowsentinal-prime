package com.shadowsentinel.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class AuthResponse {

    private String token;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expiresAt;

    public AuthResponse() {
    }

    public AuthResponse(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private Instant expiresAt;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(token, expiresAt);
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
