package sn.samabank.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.samabank.auth.entity.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
