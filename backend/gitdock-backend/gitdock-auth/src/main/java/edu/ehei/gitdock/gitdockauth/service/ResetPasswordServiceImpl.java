package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockauth.model.PasswordResetToken;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.PasswordResetTokenRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import edu.ehei.gitdock.gitdockauth.service.interfaces.ResetPasswordService;
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

/**
 * Service gérant le processus complet de réinitialisation de mot de passe.
 * Il s'occupe de la demande initiale (génération de token + envoi mail)
 * et de la confirmation (validation token + changement de mot de passe).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    // URL du frontend pour la page de réinitialisation (ex: http://localhost:5173/reset-password)
    @Value("${application.mail.reset-password-url:http://localhost:5173/reset-password}")
    private String resetPasswordUrl;

    /**
     * Initialise la procédure de réinitialisation.
     * Si l'email correspond à un utilisateur actif, un token est généré et envoyé par email.
     * * @param email L'email saisi par l'utilisateur.
     */
    @Override
    @Transactional
    public void initiate(String email) {
        // On cherche l'utilisateur. S'il n'existe pas ou est supprimé, on ne fait rien.
        // C'est une bonne pratique de sécurité pour éviter l'énumération des comptes (user enumeration attack).
        userAccountRepository.findByEmailAndIsDeletedFalse(email).ifPresent(user -> {
            // tokenRepository.deleteByUserId(user.getId());
            // 1. Génération d'un token aléatoire unique (UUID)
            String token = UUID.randomUUID().toString();

            // 2. Création et configuration de l'entité Token
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1)) // Validité courte (1h) par sécurité
                    .build();

            // 3. Persistance du token en base
            tokenRepository.save(resetToken);

            // 4. Construction du lien et envoi de l'email
            String resetLink = resetPasswordUrl + "?token=" + token;
            // emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), user.getLastName(), resetLink);

            // 👇 NOUVEAU : Création de l'événement
            Map<String, String> payload = Map.of(
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName", user.getLastName() != null ? user.getLastName() : "",
                    "resetLink", resetLink
            );

            NotificationEventDTO event = NotificationEventDTO.builder()
                    .targetUserId(user.getId())
                    .targetEmail(user.getEmail())
                    .type("TYPE_PASSWORD_RESET_REQUESTED")
                    .payload(payload)
                    .build();

            rabbitTemplate.convertAndSend("gitdock.exchange", "notification.routing.key", event);
            log.info("📢 Événement PASSWORD_RESET_REQUESTED envoyé pour {}", user.getEmail());

        });
    }

    /**
     * Valide le token et met à jour le mot de passe de l'utilisateur.
     *
     * @param token Le token reçu par email.
     * @param newPassword Le nouveau mot de passe brut (non hashé).
     * @throws RuntimeException Si le token est invalide ou expiré.
     */
    @Override
    @Transactional
    public void confirm(String token, String newPassword) {
        // 1. Recherche du token en base
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        // 2. Vérification de la date d'expiration
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Nettoyage : on supprime le token périmé pour ne pas polluer la base
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token expiré");
        }

        // 3. Récupération de l'utilisateur associé
        UserAccount user = resetToken.getUser();

        // 4. Hashage et mise à jour du mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);

        // 5. Suppression du token (Usage unique : le token ne doit plus être réutilisable)
        tokenRepository.delete(resetToken);
    }
}