package sn.samabank.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private int version;

    protected User() {}

    public static User create(String username, String email, String passwordHash, Role role) {
        User user = new User();
        user.username = username.toLowerCase().trim();
        user.email = email.toLowerCase().trim();
        user.passwordHash = passwordHash;
        user.role = role;
        user.status = UserStatus.ACTIVE;
        user.failedAttempts = 0;
        user.passwordChangeRequired = true;
        return user;
    }

    public void recordSuccessfulLogin() {
        this.failedAttempts = 0;
        this.lastLoginAt = Instant.now();
    }

    public void recordFailedLogin() {
        this.failedAttempts = this.failedAttempts + 1;
        if (this.failedAttempts >= 5) {
            this.status = UserStatus.LOCKED;
        }
    }

    public void unlock() {
        this.status = UserStatus.ACTIVE;
        this.failedAttempts = 0;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.status == UserStatus.LOCKED;
    }

    public void markPasswordAsChanged() {
        this.passwordChangeRequired = false;
    }

    public boolean hasRole(Role role) {
        return this.role == role;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void updateEmail(String email) {
        this.email = email.toLowerCase().trim();
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Getters
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public int getFailedAttempts() { return failedAttempts; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
