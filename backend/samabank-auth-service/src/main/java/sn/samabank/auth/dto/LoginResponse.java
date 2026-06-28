package sn.samabank.auth.dto;

public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;
    private final UserInfo user;
    private final boolean passwordChangeRequired;

    private LoginResponse(Builder builder) {
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.tokenType = builder.tokenType;
        this.expiresIn = builder.expiresIn;
        this.user = builder.user;
        this.passwordChangeRequired = builder.passwordChangeRequired;
    }

    public static Builder builder() { return new Builder(); }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public UserInfo getUser() { return user; }
    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserInfo user;
        private boolean passwordChangeRequired;

        public Builder accessToken(String v) { this.accessToken = v; return this; }
        public Builder refreshToken(String v) { this.refreshToken = v; return this; }
        public Builder tokenType(String v) { this.tokenType = v; return this; }
        public Builder expiresIn(long v) { this.expiresIn = v; return this; }
        public Builder user(UserInfo v) { this.user = v; return this; }
        public Builder passwordChangeRequired(boolean v) { this.passwordChangeRequired = v; return this; }
        public LoginResponse build() { return new LoginResponse(this); }
    }
}
