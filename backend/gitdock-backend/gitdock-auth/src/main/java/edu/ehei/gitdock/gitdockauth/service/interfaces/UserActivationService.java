package edu.ehei.gitdock.gitdockauth.service.interfaces;

import edu.ehei.gitdock.gitdockauth.dto.ActivateAccountRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.SetPasswordRequestDTO;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;

/**
 * Interface définissant le contrat de service pour l'activation des comptes utilisateurs.
 * <p>
 * Elle gère le cycle de vie complet de l'activation :
 * 1. Génération et envoi du token (lors de l'inscription).
 * 2. Validation du token (lors du clic sur le lien).
 * 3. Finalisation (définition du mot de passe).
 * </p>
 */
public interface UserActivationService {

    /**
     * Génère un token d'activation et envoie l'email correspondant à l'utilisateur.
     * <p>
     * Cette méthode est généralement appelée juste après la persistance initiale
     * de l'utilisateur (statut inactif) lors de l'inscription.
     * </p>
     *
     * @param user L'entité utilisateur venant d'être créée.
     */
    void sendActivationEmail(UserAccount user);

    /**
     * Valide le token et active définitivement le compte utilisateur.
     * <p>
     * Cette étape inclut la définition du mot de passe choisi par l'utilisateur.
     * Une fois cette méthode exécutée avec succès, l'utilisateur peut se connecter.
     * </p>
     *
     * @param request Le DTO contenant le token et le nouveau mot de passe.
     */
    void activateUser(SetPasswordRequestDTO request);

    /**
     * Relance le processus d'envoi d'email d'activation.
     * <p>
     * Utile si le lien précédent a expiré (token périmé) ou si l'utilisateur
     * a perdu l'email. Elle génère un NOUVEAU token pour remplacer l'ancien.
     * </p>
     *
     * @param email L'adresse email de l'utilisateur demandant le renvoi.
     */
    void resendActivationEmail(String email);

    /**
     * Vérifie simplement si un token est valide (existant, non utilisé, non expiré).
     * <p>
     * Cette méthode est souvent utilisée par le frontend au chargement de la page
     * pour afficher une erreur immédiate si le lien est mort, avant même de
     * demander le mot de passe.
     * </p>
     *
     * @param token La chaîne de caractères du token.
     * @return true si le token est valide, false sinon.
     */
    boolean isValidActivationToken(String token);

    void activateInvitedAccount(ActivateAccountRequestDTO request);

}