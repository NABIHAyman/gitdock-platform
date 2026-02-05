package com.auth.backendauth.repository;

import com.auth.backendauth.model.PasswordResetToken;
import com.auth.backendauth.model.UserAccount;
import com.auth.backendauth.model.ValidationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationTokenRepository extends JpaRepository<ValidationToken, Long> {
    Optional<ValidationToken> findByTokenValue(String tokenValue);
    Optional<ValidationToken> findByUser(UserAccount user);
}
