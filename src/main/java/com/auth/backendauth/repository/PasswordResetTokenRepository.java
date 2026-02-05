package com.auth.backendauth.repository;

import com.auth.backendauth.model.PasswordResetToken;
import com.auth.backendauth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(UserAccount user);
}
