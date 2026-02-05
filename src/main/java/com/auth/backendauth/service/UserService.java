package com.auth.backendauth.service;

import com.auth.backendauth.dto.RegisterRequest;
import com.auth.backendauth.model.PasswordResetToken;
import com.auth.backendauth.model.UserAccount;
import com.auth.backendauth.model.ValidationToken;
import com.auth.backendauth.repository.PasswordResetTokenRepository;
import com.auth.backendauth.repository.UserAccountRepository;
import com.auth.backendauth.repository.ValidationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private ValidationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ================= REGISTER =================
    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        UserAccount user = new UserAccount();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        userRepository.save(user);

        String tokenValue = UUID.randomUUID().toString();

        ValidationToken token = new ValidationToken();
        token.setTokenValue(tokenValue);
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setUsed(false);

        tokenRepository.save(token);

        emailService.sendActivationEmail(user.getEmail(), tokenValue);
    }

    // ================= ACTIVATE ACCOUNT =================
    public void activateAccount(String tokenValue) {

        ValidationToken token = tokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        if (token.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        UserAccount user = token.getUser();
        user.setEnabled(true);

        token.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(token);
    }
    // ================= Login =================
    public UserAccount login(String email, String password) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!user.getEnabled()) {
            throw new RuntimeException("Account not activated. Check your email.");
        }

        if (user.getAccountLocked()) {
            throw new RuntimeException("Account is locked.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Incorrect password");
        }

        return user; // tu peux retourner l'objet ou un token JWT
    }
    // ================= SendResetPassword =================
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public void sendResetPasswordEmail(String email) {

        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        passwordResetTokenRepository.findByUser(user)
                .ifPresent(passwordResetTokenRepository::delete);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);

        //  on définit expires_at
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
    }

    // ================= ResetPassword =================
    public void resetPassword(String tokenValue, String newPassword) {

        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (token.getUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        UserAccount user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        token.setUsed(true);

        userRepository.save(user);
        passwordResetTokenRepository.save(token);
    }

}
