package sn.samabank.auth.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.auth.client.AuditServiceClient;
import sn.samabank.auth.client.NotificationServiceClient;
import sn.samabank.auth.client.dto.CreateAuditEventRequest;
import sn.samabank.auth.client.dto.SendEmailRequest;
import sn.samabank.auth.config.SamaBankProperties;
import sn.samabank.auth.dto.*;
import sn.samabank.auth.entity.PasswordResetToken;
import sn.samabank.auth.entity.RefreshToken;
import sn.samabank.auth.entity.User;
import sn.samabank.auth.repository.PasswordResetTokenRepository;
import sn.samabank.auth.repository.RefreshTokenRepository;
import sn.samabank.auth.repository.UserRepository;
import sn.samabank.auth.security.JwtService;
import sn.samabank.auth.shared.BusinessException;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SamaBankProperties properties;
    private final AuditServiceClient auditServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       SamaBankProperties properties,
                       AuditServiceClient auditServiceClient,
                       NotificationServiceClient notificationServiceClient) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.auditServiceClient = auditServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse login(LoginRequest request, String ipAddress, String channel) {
        String username = request.getUsername().toLowerCase().trim();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[AUTH] Login failed - user not found: {} from IP: {}", username, ipAddress);
                    return new BusinessException(
                            "INVALID_CREDENTIALS",
                            "Identifiant ou mot de passe incorrect",
                            HttpStatus.UNAUTHORIZED
                    );
                });

        if (user.isLocked()) {
            log.warn("[AUTH] Login blocked - locked account: {} from IP: {}", username, ipAddress);
            logAudit("LOGIN_BLOCKED", user.getId(), user.getRole().name(),
                    "User", user.getId(), null, ipAddress, channel,
                    Map.of("reason", "ACCOUNT_LOCKED", "username", username));
            throw new BusinessException(
                    "ACCOUNT_LOCKED",
                    "Compte verrouille apres 5 tentatives echouees",
                    HttpStatus.FORBIDDEN
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int rowsUpdated = userRepository.incrementFailedAttemptsNative(user.getId());
            log.info("[AUTH] Native query updated {} rows for user {}", rowsUpdated, user.getId());

            entityManager.clear();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(
                            "INVALID_CREDENTIALS",
                            "Identifiant ou mot de passe incorrect",
                            HttpStatus.UNAUTHORIZED
                    ));

            log.warn("[AUTH] Login failed - wrong password: {} from IP: {} (attempts: {})",
                    username, ipAddress, user.getFailedAttempts());

            logAudit("LOGIN_FAILED", user.getId(), user.getRole().name(),
                    "User", user.getId(), null, ipAddress, channel,
                    Map.of("attempt", String.valueOf(user.getFailedAttempts()), "username", username));

            if (user.isLocked()) {
                log.error("[AUTH] Account locked after failed attempts: {} from IP: {}", username, ipAddress);
                logAudit("ACCOUNT_LOCKED", user.getId(), user.getRole().name(),
                        "User", user.getId(), null, ipAddress, channel,
                        Map.of("failedAttempts", String.valueOf(user.getFailedAttempts()), "username", username));
                throw new BusinessException(
                        "ACCOUNT_LOCKED",
                        "Compte verrouille apres 5 tentatives echouees",
                        HttpStatus.FORBIDDEN
                );
            }

            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "Identifiant ou mot de passe incorrect",
                    HttpStatus.UNAUTHORIZED
            );
        }

        userRepository.unlockUser(user.getId());
        userRepository.updateLastLogin(user.getId(), Instant.now());
        entityManager.clear();
        user = userRepository.findByUsername(username).orElseThrow();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateAndSaveRefreshToken(user);

        log.info("[AUTH] Login success - user: {} role: {} from IP: {} channel: {}",
                user.getUsername(), user.getRole(), ipAddress, channel);

        logAudit("LOGIN_SUCCESS", user.getId(), user.getRole().name(),
                "User", user.getId(), null, ipAddress, channel,
                Map.of("username", user.getUsername()));

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(properties.getJwt().getAccessTokenTtl())
                .user(UserInfo.from(user))
                .passwordChangeRequired(user.isPasswordChangeRequired())
                .build();
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request, String ipAddress, String channel) {
        String rawRefreshToken = request.getRefreshToken();

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException(
                    "MISSING_REFRESH_TOKEN",
                    "Le refresh token est obligatoire",
                    HttpStatus.BAD_REQUEST
            );
        }

        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(
                        "INVALID_REFRESH_TOKEN",
                        "Refresh token invalide",
                        HttpStatus.UNAUTHORIZED
                ));

        if (refreshToken.isRevoked()) {
            throw new BusinessException(
                    "REFRESH_TOKEN_REVOKED",
                    "Refresh token revoque - veuillez vous reconnecter",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (refreshToken.isExpired()) {
            throw new BusinessException(
                    "REFRESH_TOKEN_EXPIRED",
                    "Refresh token expire - veuillez vous reconnecter",
                    HttpStatus.UNAUTHORIZED
            );
        }

        User user = refreshToken.getUser();

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = generateAndSaveRefreshToken(user);

        log.info("[AUTH] Token refreshed - user: {} from IP: {}", user.getUsername(), ipAddress);

        logAudit("TOKEN_REFRESHED", user.getId(), user.getRole().name(),
                "User", user.getId(), null, ipAddress, channel,
                Map.of("username", user.getUsername()));

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(properties.getJwt().getAccessTokenTtl())
                .user(UserInfo.from(user))
                .build();
    }

    @Transactional
    public void logout(UUID userId, String ipAddress, String channel) {
        User user = userRepository.findById(userId).orElse(null);
        String role = user != null ? user.getRole().name() : "UNKNOWN";
        String username = user != null ? user.getUsername() : "UNKNOWN";

        refreshTokenRepository.revokeAllByUserId(userId);

        log.info("[AUTH] Logout - userId: {} from IP: {}", userId, ipAddress);

        logAudit("LOGOUT", userId, role, "User", userId, null, ipAddress, channel,
                Map.of("username", username));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request,
                               String ipAddress, String channel) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilisateur", userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            logAudit("PASSWORD_CHANGE_FAILED", userId, user.getRole().name(),
                    "User", userId, null, ipAddress, channel,
                    Map.of("reason", "INVALID_OLD_PASSWORD", "username", user.getUsername()));
            throw new BusinessException(
                    "INVALID_OLD_PASSWORD",
                    "L'ancien mot de passe est incorrect",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    "SAME_PASSWORD",
                    "Le nouveau mot de passe doit etre different de l'ancien",
                    HttpStatus.CONFLICT
            );
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        user.markPasswordAsChanged();
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(userId);

        log.info("[AUTH] Password changed - user: {}", user.getUsername());

        logAudit("PASSWORD_CHANGED", userId, user.getRole().name(),
                "User", userId, null, ipAddress, channel,
                Map.of("username", user.getUsername()));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String ipAddress, String channel) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.info("[AUTH] Forgot password - email not found: {} from IP: {}", email, ipAddress);
            return;
        }

        passwordResetTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()) && t.isValid())
                .forEach(t -> {
                    t.markAsUsed();
                    passwordResetTokenRepository.save(t);
                });

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(3600);

        PasswordResetToken resetToken = PasswordResetToken.create(user, tokenHash, expiresAt);
        passwordResetTokenRepository.save(resetToken);

        log.info("[AUTH] Password reset token generated - user: {} from IP: {}", user.getUsername(), ipAddress);

        String resetLink = properties.getFrontend().getBaseUrl() + "/reset-password?token=" + rawToken;

        // Send email via notification service
        try {
            notificationServiceClient.sendEmail(new SendEmailRequest(
                    user.getEmail(),
                    user.getUsername(),
                    "PASSWORD_RESET",
                    "Reinitialisation de votre mot de passe SamaBank",
                    buildPasswordResetEmailBody(user.getUsername(), resetLink)
            ));
        } catch (Exception e) {
            log.warn("[AUTH] Could not send password reset email: {}", e.getMessage());
        }

        logAudit("PASSWORD_RESET_REQUESTED", user.getId(), user.getRole().name(),
                "User", user.getId(), null, ipAddress, channel,
                Map.of("username", user.getUsername(), "email", email));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, String ipAddress, String channel) {
        String tokenHash = hashToken(request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(
                        "INVALID_RESET_TOKEN",
                        "Token de reinitialisation invalide",
                        HttpStatus.UNAUTHORIZED
                ));

        if (resetToken.isUsed()) {
            throw new BusinessException(
                    "RESET_TOKEN_USED",
                    "Ce token a deja ete utilise",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (resetToken.isExpired()) {
            throw new BusinessException(
                    "RESET_TOKEN_EXPIRED",
                    "Token de reinitialisation expire",
                    HttpStatus.UNAUTHORIZED
            );
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    "SAME_PASSWORD",
                    "Le nouveau mot de passe doit etre different de l'ancien",
                    HttpStatus.CONFLICT
            );
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        user.markPasswordAsChanged();
        user.unlock();
        userRepository.save(user);

        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("[AUTH] Password reset completed - user: {} from IP: {}", user.getUsername(), ipAddress);

        logAudit("PASSWORD_RESET_COMPLETED", user.getId(), user.getRole().name(),
                "User", user.getId(), null, ipAddress, channel,
                Map.of("username", user.getUsername()));
    }

    // ── Privates ────────────────────────────────────────────────────

    private String generateAndSaveRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(properties.getJwt().getRefreshTokenTtl());

        RefreshToken refreshToken = RefreshToken.create(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        if (rawToken == null) throw new IllegalArgumentException("rawToken cannot be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    private void logAudit(String eventType, UUID actorId, String actorRole,
                          String resourceType, UUID resourceId, UUID correlationId,
                          String ipAddress, String channel, Map<String, Object> payload) {
        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    eventType, actorId, actorRole, resourceType, resourceId,
                    correlationId, ipAddress, channel, payload
            ));
        } catch (Exception e) {
            log.warn("[AUTH] Could not log audit event {}: {}", eventType, e.getMessage());
        }
    }

    private String buildPasswordResetEmailBody(String username, String resetLink) {
        return """
            <html><body>
            <p>Bonjour <strong>%s</strong>,</p>
            <p>Vous avez demande la reinitialisation de votre mot de passe SamaBank.</p>
            <p><a href="%s">Reinitialiser mon mot de passe</a></p>
            <p>Ce lien est valable pendant 1 heure.</p>
            </body></html>
            """.formatted(username, resetLink);
    }
}
