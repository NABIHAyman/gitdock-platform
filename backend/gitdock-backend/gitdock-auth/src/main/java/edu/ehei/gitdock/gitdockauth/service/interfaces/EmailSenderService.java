package edu.ehei.gitdock.gitdockauth.service.interfaces;

/**
 * Interface définissant le contrat pour l'envoi d'emails transactionnels.
 * <p>
 * Cette abstraction permet de découpler la logique métier (AuthenticationService)
 * de la technologie d'envoi d'emails (JavaMail, SendGrid, Mailgun, etc.).
 * L'implémentation (EmailSenderServiceImpl) se chargera des détails techniques.
 * </p>
 */
public interface EmailSenderService {

    /**
     * Envoie un email contenant un lien d'activation de compte.
     * <p>
     * Cet email est déclenché immédiatement après l'inscription (Register).
     * Le lien doit être cliquable et contenir un token unique.
     * </p>
     *
     * @param toEmail        L'adresse email du destinataire.
     * @param firstName      Le prénom (pour personnaliser le message, ex: "Bonjour Jean").
     * @param lastName       Le nom de famille.
     * @param activationLink L'URL complète vers laquelle l'utilisateur sera redirigé.
     */
    void sendActivationEmail(String toEmail, String firstName, String lastName, String activationLink);

    /**
     * Envoie un email contenant un lien de réinitialisation de mot de passe.
     * <p>
     * Cet email est déclenché par la procédure "Mot de passe oublié".
     * Le lien est temporaire et expire généralement rapidement (ex: 15-30 min).
     * </p>
     *
     * @param toEmail   L'adresse email du destinataire.
     * @param firstName Le prénom.
     * @param lastName  Le nom.
     * @param resetLink L'URL complète contenant le token de réinitialisation.
     */
    void sendPasswordResetEmail(String toEmail, String firstName, String lastName, String resetLink);
}