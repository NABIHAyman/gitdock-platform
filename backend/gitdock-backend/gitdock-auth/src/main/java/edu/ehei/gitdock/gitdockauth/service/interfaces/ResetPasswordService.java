package edu.ehei.gitdock.gitdockauth.service.interfaces;

/**
 * Interface définissant le contrat métier pour la fonctionnalité de réinitialisation de mot de passe.
 * <p>
 * Le processus est découpé en deux étapes distinctes :
 * 1. L'initiation (demande de reset par email).
 * 2. La confirmation (changement effectif du mot de passe via le token reçu).
 * </p>
 */
public interface ResetPasswordService {

    /**
     * Démarre la procédure de réinitialisation.
     * <p>
     * Cette méthode doit :
     * 1. Vérifier si un compte existe avec cet email.
     * 2. Générer un token sécurisé à usage unique.
     * 3. Envoyer un email contenant le lien de réinitialisation.
     * </p>
     *
     * @param email L'adresse email saisie par l'utilisateur dans le formulaire "Mot de passe oublié".
     */
    void initiate(String email);

    /**
     * Finalise la procédure de réinitialisation.
     * <p>
     * Cette méthode doit :
     * 1. Valider le token (existence, non utilisé, non expiré).
     * 2. Mettre à jour le mot de passe de l'utilisateur (en le hachant).
     * 3. Invalider le token pour empêcher sa réutilisation.
     * </p>
     *
     * @param token       Le jeton de sécurité extrait de l'URL.
     * @param newPassword Le nouveau mot de passe brut choisi par l'utilisateur.
     */
    void confirm(String token, String newPassword);
}