package sn.samabank.auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.samabank.auth.entity.Role;
import sn.samabank.auth.entity.User;
import sn.samabank.auth.entity.UserStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0, u.status = sn.samabank.auth.entity.UserStatus.ACTIVE WHERE u.id = :userId")
    void unlockUser(UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :now WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET failed_attempts = failed_attempts + 1, " +
            "status = CASE WHEN failed_attempts + 1 >= 5 THEN 'LOCKED' ELSE status END, " +
            "updated_at = NOW() " +
            "WHERE id = :userId", nativeQuery = true)
    int incrementFailedAttemptsNative(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.status = sn.samabank.auth.entity.UserStatus.LOCKED WHERE u.id = :userId AND u.failedAttempts >= 5")
    void lockUserIfNeeded(@Param("userId") UUID userId);

    @Query(value = """
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
          AND (:status IS NULL OR u.status = :status)
          AND (:search IS NULL
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        """,
            countQuery = """
        SELECT COUNT(u) FROM User u
        WHERE (:role IS NULL OR u.role = :role)
          AND (:status IS NULL OR u.status = :status)
          AND (:search IS NULL
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        """)
    Page<User> findAllWithFilters(
            @Param("role") Role role,
            @Param("status") UserStatus status,
            @Param("search") String search,
            Pageable pageable);

    long countByRole(Role role);
}
