package sn.samabank.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.auth.client.AuditServiceClient;
import sn.samabank.auth.client.dto.CreateAuditEventRequest;
import sn.samabank.auth.dto.CreateUserRequest;
import sn.samabank.auth.dto.UpdateUserRequest;
import sn.samabank.auth.dto.UserResponse;
import sn.samabank.auth.entity.Role;
import sn.samabank.auth.entity.User;
import sn.samabank.auth.entity.UserStatus;
import sn.samabank.auth.repository.UserRepository;
import sn.samabank.auth.shared.BusinessException;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditServiceClient auditServiceClient;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditServiceClient auditServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditServiceClient = auditServiceClient;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request,
                                   Role role,
                                   UUID createdBy,
                                   String actorRole,
                                   String executorUsername,
                                   String channel,
                                   String ipAddress) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "EMAIL_ALREADY_EXISTS",
                    "Un utilisateur avec cet email existe deja",
                    HttpStatus.CONFLICT
            );
        }

        String username = generateUsername(request.getFirstName(), request.getLastName());
        User user = User.create(
                username,
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        userRepository.save(user);

        String eventType = role == Role.TELLER ? "TELLER_CREATED" : "ADMIN_CREATED";

        log.info("[USER] {} - username: {} email: {} by: {} ({})",
                eventType, username, user.getEmail(), executorUsername, createdBy);

        logAudit(eventType, createdBy, actorRole, "User", user.getId(), null, ipAddress, channel,
                Map.of("username", username, "email", user.getEmail(),
                        "role", role.name(), "executedByUsername", executorUsername));

        return UserResponse.from(user);
    }

    public Page<UserResponse> getAll(Role role, UserStatus status, String search, Pageable pageable) {
        return userRepository.findAllWithFilters(role, status, search, pageable)
                .map(UserResponse::from);
    }

    public UserResponse getById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilisateur", userId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(UUID userId,
                               UpdateUserRequest request,
                               UUID updatedBy,
                               String actorRole,
                               String executorUsername,
                               String channel,
                               String ipAddress) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilisateur", userId));

        String oldEmail = user.getEmail();
        boolean emailChanged = false;
        boolean passwordChanged = false;

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("EMAIL_EXISTS", "Email deja utilise", HttpStatus.CONFLICT);
            }
            user.updateEmail(request.getEmail());
            emailChanged = true;
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
            passwordChanged = true;
        }

        userRepository.save(user);

        log.info("[USER] Modifie - id: {} emailChanged: {} pwdChanged: {} by: {} ({})",
                userId, emailChanged, passwordChanged, executorUsername, updatedBy);

        logAudit("USER_UPDATED", updatedBy, actorRole, "User", user.getId(), null, ipAddress, channel,
                Map.of("username", user.getUsername(), "role", user.getRole().name(),
                        "oldEmail", oldEmail, "newEmail", user.getEmail(),
                        "emailChanged", String.valueOf(emailChanged),
                        "passwordChanged", String.valueOf(passwordChanged),
                        "executedByUsername", executorUsername));

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse suspend(UUID userId, UUID suspendedBy, String actorRole,
                                String executorUsername, String channel, String ipAddress) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilisateur", userId));

        if (userId.equals(suspendedBy)) {
            throw new BusinessException(
                    "SELF_SUSPENSION",
                    "Vous ne pouvez pas vous suspendre vous-meme",
                    HttpStatus.FORBIDDEN
            );
        }

        user.suspend();
        userRepository.save(user);

        log.info("[USER] Suspendu - id: {} username: {} by: {} ({})",
                userId, user.getUsername(), executorUsername, suspendedBy);

        logAudit("USER_SUSPENDED", suspendedBy, actorRole, "User", user.getId(), null, ipAddress, channel,
                Map.of("username", user.getUsername(), "role", user.getRole().name(),
                        "previousStatus", "ACTIVE", "executedByUsername", executorUsername));

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse unlock(UUID userId, UUID unlockedBy, String actorRole,
                               String executorUsername, String channel, String ipAddress) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilisateur", userId));

        UserStatus previousStatus = user.getStatus();

        user.unlock();
        userRepository.save(user);

        log.info("[USER] Deverrouille - id: {} username: {} by: {} ({})",
                userId, user.getUsername(), executorUsername, unlockedBy);

        logAudit("USER_UNLOCKED", unlockedBy, actorRole, "User", user.getId(), null, ipAddress, channel,
                Map.of("username", user.getUsername(), "role", user.getRole().name(),
                        "previousStatus", previousStatus.name(), "executedByUsername", executorUsername));

        return UserResponse.from(user);
    }

    @Transactional
    public void delete(UUID userId, UUID deletedBy, String actorRole,
                       String executorUsername, String channel, String ipAddress) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("Utilisateur", userId));

        if (userId.equals(deletedBy)) {
            throw new BusinessException(
                    "SELF_DELETION",
                    "Vous ne pouvez pas vous supprimer vous-meme",
                    HttpStatus.FORBIDDEN
            );
        }

        String username = user.getUsername();
        String role = user.getRole().name();

        userRepository.delete(user);

        log.info("[USER] Supprime - id: {} username: {} by: {} ({})",
                userId, username, executorUsername, deletedBy);

        logAudit("USER_DELETED", deletedBy, actorRole, "User", userId, null, ipAddress, channel,
                Map.of("username", username, "role", role, "executedByUsername", executorUsername));
    }

    // ── Stats internes ────────────────────────────────────────────

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    // ── Utilitaires ────────────────────────────────────────────────

    private String generateUsername(String firstName, String lastName) {
        String base = (firstName.charAt(0) + lastName).toLowerCase().replaceAll("[^a-z]", "");
        String username = base;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + suffix++;
        }
        return username;
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
            log.warn("[USER] Could not log audit event {}: {}", eventType, e.getMessage());
        }
    }
}
