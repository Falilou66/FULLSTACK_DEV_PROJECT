package sn.samabank.auth.dto;

import sn.samabank.auth.entity.User;

import java.util.UUID;

public class UserInfo {
    private final UUID id;
    private final String username;
    private final String email;
    private final String role;

    private UserInfo(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.role = builder.role;
    }

    public static UserInfo from(User user) {
        return builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String username;
        private String email;
        private String role;

        public Builder id(UUID val) { this.id = val; return this; }
        public Builder username(String val) { this.username = val; return this; }
        public Builder email(String val) { this.email = val; return this; }
        public Builder role(String val) { this.role = val; return this; }
        public UserInfo build() { return new UserInfo(this); }
    }
}
