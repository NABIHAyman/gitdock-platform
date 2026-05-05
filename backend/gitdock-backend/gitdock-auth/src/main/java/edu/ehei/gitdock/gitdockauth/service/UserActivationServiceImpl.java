package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.dto.ActivateAccountRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockauth.dto.SetPasswordRequestDTO;
import edu.ehei.gitdock.gitdockauth.model.ActivationToken;
import edu.ehei.gitdock.gitdockauth.model.InvitationToken;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.ActivationTokenRepository;
import edu.ehei.gitdock.gitdockauth.repository.InvitationTokenRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import edu.ehei.gitdock.gitdockauth.service.interfaces.UserActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivationServiceImpl implements UserActivationService {

    private final ActivationTokenRepository activationTokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationTokenRepository invitationTokenRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${application.mail.activation-url:http://localhost:5173/activate}")
    private String activationUrl;

    @Value("${application.security.activation.token.expiration:24}")
    private int tokenExpirationHours;

    @Override
    @Transactional
    public void sendActivationEmail(UserAccount user) {
        try {
            activationTokenRepository.deleteByUser_Id(user.getId());
        } catch (Exception e) {
            log.warn("Could not delete old tokens for user {}", user.getId());
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusHours(tokenExpirationHours);

        ActivationToken activationToken = ActivationToken.builder()
                .token(token)
                .user(user)
                .createdAt(now)
                .expiresAt(expiryDate)
                .used(false)
                .build();

        activationTokenRepository.save(activationToken);

        String activationLink = activationUrl + "?token=" + token;

        Map<String, String> payload = Map.of(
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "lastName", user.getLastName() != null ? user.getLastName() : "",
                "activationLink", activationLink
        );

        NotificationEventDTO event = NotificationEventDTO.builder()
                .targetUserId(user.getId())
                .targetEmail(user.getEmail())
                .type("TYPE_USER_REGISTERED")
                .payload(payload)
                .build();

        rabbitTemplate.convertAndSend("gitdock.exchange", "notification.routing.key", event);
        log.info("Activation email sent to {} with token: {}", user.getEmail(), token);
    }

    @Override
    @Transactional
    public void activateUser(SetPasswordRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        ActivationToken token = activationTokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(
                request.getToken(),
                LocalDateTime.now()
        ).orElseThrow(() -> new RuntimeException("Token d'activation invalide ou expiré"));

        UserAccount user = token.getUser();
        user.setEnabled(true);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        token.setUsed(true);
        activationTokenRepository.save(token);
        userAccountRepository.save(user);

        log.info("User {} activated successfully", user.getEmail());
    }

    @Override
    @Transactional
    public void resendActivationEmail(String email) {
        var user = userAccountRepository.findByEmailAndIsDeletedFalse(email)
                .orElse(null);

        if (user == null) {
            log.warn("Email inconnu : {}", email);
            return;
        }

        if (user.isEnabled()) {
            log.info("Demande de renvoi ignorée, compte déjà actif pour : {}", email);
            return;
        }

        sendActivationEmail(user);
    }

    @Override
    @Transactional
    public void activateInvitedAccount(ActivateAccountRequestDTO request) {
        // 1. Validation de la concordance des mots de passe
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        // 2. Nettoyage du token
        String tokenValue = (request.getToken() != null) ? request.getToken().trim() : "";
        log.debug("Tentative d'activation de compte invité avec le token : '{}'", tokenValue);

        // 3. Recherche du token d'invitation
        InvitationToken token = invitationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    log.error("ERREUR : Aucun token d'invitation trouvé pour : '{}'", tokenValue);
                    return new RuntimeException("Token d'invitation invalide");
                });

        // 4. Vérifications d'usage
        if (token.getConfirmedAt() != null) {
            throw new RuntimeException("Compte déjà activé");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token d'invitation expiré");
        }

        // 5. Mise à jour de l'invitation et de l'utilisateur
        token.setConfirmedAt(LocalDateTime.now());
        invitationTokenRepository.save(token);

        UserAccount user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        userAccountRepository.save(user);

        log.info("Compte invité activé avec succès pour : {}", user.getEmail());
    }

    @Override
    public boolean isValidActivationToken(String token) {
        return activationTokenRepository
                .findByTokenAndUsedFalseAndExpiresAtAfter(token, LocalDateTime.now())
                .isPresent();
    }
}