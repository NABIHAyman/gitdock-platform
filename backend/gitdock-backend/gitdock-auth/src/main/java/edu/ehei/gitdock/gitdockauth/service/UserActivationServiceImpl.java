package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.dto.SetPasswordRequestDTO;
import edu.ehei.gitdock.gitdockauth.model.ActivationToken;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.ActivationTokenRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import edu.ehei.gitdock.gitdockauth.service.interfaces.EmailSenderService;
import edu.ehei.gitdock.gitdockauth.service.interfaces.UserActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service gérant le cycle de vie de l'activation des comptes utilisateurs.
 * Responsabilités :
 * - Génération et envoi des tokens d'activation.
 * - Validation des tokens.
 * - Activation finale du compte avec définition du mot de passe.
 * - Gestion du renvoi d'email en cas de perte ou d'expiration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivationServiceImpl implements UserActivationService {

    private final ActivationTokenRepository activationTokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final EmailSenderService emailService;
    private final PasswordEncoder passwordEncoder;

    // URL du frontend vers laquelle l'utilisateur sera redirigé depuis l'email
    @Value("${application.mail.activation-url:http://localhost:5173/activate}")
    private String activationUrl;

    // Durée de validité du token en heures (par défaut 24h)
    @Value("${application.security.activation.token.expiration:24}")
    private int tokenExpirationHours;

    /**
     * Génère un nouveau token d'activation pour un utilisateur donné et envoie l'email.
     * Cette méthode nettoie également les anciens tokens potentiellement existants pour cet utilisateur.
     *
     * @param user L'utilisateur destinataire de l'activation.
     */
    @Override
    @Transactional
    public void sendActivationEmail(UserAccount user) {
        // 1. Nettoyage préventif : suppression des anciens tokens pour éviter les conflits ou l'accumulation
        try {
            activationTokenRepository.deleteByUser_Id(user.getId());
        } catch (Exception e) {
            // On loggue juste un avertissement car l'échec de suppression ne doit pas bloquer l'envoi du nouveau token
            log.warn("Could not delete old tokens for user {}", user.getId());
        }

        // 2. Génération du token unique (UUID) et calcul de la date d'expiration
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusHours(tokenExpirationHours);

        // 3. Persistance du token en base de données
        ActivationToken activationToken = ActivationToken.builder()
                .token(token)
                .user(user)
                .createdAt(now)
                .expiresAt(expiryDate)
                .used(false)
                .build();

        activationTokenRepository.save(activationToken);

        // 4. Construction du lien complet
        String activationLink = activationUrl + "?token=" + token;

        // 5. Appel asynchrone au service d'envoi d'email
        emailService.sendActivationEmail(user.getEmail(), user.getFirstName(), user.getLastName(), activationLink);

        log.info("Activation email sent to {} with token: {}", user.getEmail(), token);
    }

    /**
     * Active définitivement un compte utilisateur après validation du token et définition du mot de passe.
     *
     * @param request DTO contenant le token et le nouveau mot de passe choisi par l'utilisateur.
     * @throws RuntimeException Si les mots de passe ne correspondent pas ou si le token est invalide/expiré.
     */
    @Override
    @Transactional
    public void activateUser(SetPasswordRequestDTO request) {
        // 1. Vérification de la concordance des mots de passe (sécurité basique)
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        // 2. Recherche et validation du token en base
        // Le token doit exister, ne pas avoir été déjà utilisé, et ne pas être expiré.
        ActivationToken token = activationTokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(
                request.getToken(),
                LocalDateTime.now()
        ).orElseThrow(() -> new RuntimeException("Token d'activation invalide ou expiré"));

        // 3. Activation du compte utilisateur lié au token
        UserAccount user = token.getUser();
        user.setEnabled(true); // Le flag qui autorise le login

        // 4. Hashage et sauvegarde du mot de passe définitif
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 5. Invalidation du token pour empêcher sa réutilisation
        token.setUsed(true);

        // 6. Sauvegarde des changements
        activationTokenRepository.save(token);
        userAccountRepository.save(user);

        log.info("User {} activated successfully", user.getEmail());
    }

    /**
     * Relance le processus d'activation pour un utilisateur existant.
     * Utile si l'email a été perdu ou si le token précédent a expiré.
     *
     * @param email L'adresse email de l'utilisateur.
     */
    @Override
    @Transactional
    public void resendActivationEmail(String email) {
        // Recherche de l'utilisateur (on ignore les comptes supprimés logiquement)
        var user = userAccountRepository.findByEmailAndIsDeletedFalse(email)
                .orElse(null);

        // Sécurité : Si l'email n'existe pas, on ne fait rien (pour éviter l'énumération des utilisateurs)
        // Mais on loggue un warning pour le monitoring
        if (user == null) {
            log.warn("Email inconnu : {}", email);
            return;
        }

        // Si le compte est déjà actif, pas besoin de renvoyer un lien d'activation
        if (user.isEnabled()) {
            log.info("Demande de renvoi ignorée, compte déjà actif pour : {}", email);
            return;
        }

        // Si tout est OK, on relance la procédure standard
        sendActivationEmail(user);
    }

    /**
     * Vérifie la validité d'un token sans le consommer.
     * Cette méthode est souvent utilisée par le frontend au chargement de la page d'activation
     * pour afficher un message d'erreur immédiat si le lien est mort.
     *
     * @param token Le token à vérifier.
     * @return true si le token est valide et utilisable, false sinon.
     */
    @Override
    public boolean isValidActivationToken(String token) {
        return activationTokenRepository
                .findByTokenAndUsedFalseAndExpiresAtAfter(token, LocalDateTime.now())
                .isPresent();
    }
}