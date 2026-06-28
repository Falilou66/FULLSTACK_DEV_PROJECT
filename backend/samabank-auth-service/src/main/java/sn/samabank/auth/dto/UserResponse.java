package sn.samabank.auth.dto;

import sn.samabank.auth.entity.User;
import sn.samabank.auth.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public class UserResponse {

    private final UUID id;
    private final String username;
    private final String email;
    private final String role;
    private final UserStatus status;
    private final int failedAttempts;
    private final Instant lastLoginAt;
    private final Instant createdAt;

    private UserResponse(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.role = builder.role;
        this.status = builder.status;
        this.failedAttempts = builder.failedAttempts;
        this.lastLoginAt = builder.lastLoginAt;
        this.createdAt = builder.createdAt;
    }

    public static UserResponse from(User user) {
        return new Builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus())
                .failedAttempts(user.getFailedAttempts())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public int getFailedAttempts() { return failedAttempts; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public Instant getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String username;
        private String email;
        private String role;
        private UserStatus status;
        private int failedAttempts;
        private Instant lastLoginAt;
        private Instant createdAt;

        public Builder id(UUID v) { this.id = v; return this; }
        public Builder username(String v) { this.username = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder role(String v) { this.role = v; return this; }
        public Builder status(UserStatus v) { this.status = v; return this; }
        public Builder failedAttempts(int v) { this.failedAttempts = v; return this; }
        public Builder lastLoginAt(Instant v) { this.lastLoginAt = v; return this; }
        public Builder createdAt(Instant v) { this.createdAt = v; return this; }
        public UserResponse build() { return new UserResponse(this); }
    }
}
