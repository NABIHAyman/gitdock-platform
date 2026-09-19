package edu.ehei.gitdock.gitdockauth.service.interfaces;

import edu.ehei.gitdock.gitdockauth.dto.AuthenticationRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.AuthenticationResponseDTO;
import edu.ehei.gitdock.gitdockauth.dto.RegisterRequestDTO;

/**
 * Interface définissant le contrat de service pour l'authentification.
 * <p>
 * Elle expose les méthodes métier de haut niveau pour gérer le cycle de vie
 * de l'accès utilisateur (Inscription et Connexion).
 * L'implémentation de cette interface (AuthenticationServiceImpl) contiendra
 * la logique réelle (appels aux repositories, hachage, génération de token, etc.).
 * </p>
 */
public interface AuthenticationService {

    /**
     * Gère l'inscription d'un nouvel utilisateur.
     * <p>
     * Cette méthode doit orchestrer :
     * 1. La vérification de l'unicité de l'email.
     * 2. La création du compte utilisateur (initialement inactif).
     * 3. La création de l'espace de travail (Company) personnel par défaut.
     * 4. L'envoi de l'email d'activation.
     * </p>
     *
     * @param request Le DTO contenant les informations du formulaire d'inscription.
     */
    void register(RegisterRequestDTO request);

    /**
     * Authentifie un utilisateur et génère les jetons de sécurité.
     * <p>
     * Cette méthode doit :
     * 1. Vérifier la validité des identifiants (Email / Mot de passe).
     * 2. S'assurer que le compte est actif et non banni.
     * 3. Générer les tokens JWT (Access Token & Refresh Token).
     * </p>
     *
     * @param request Le DTO contenant l'email et le mot de passe.
     * @return Un DTO complet contenant les tokens JWT et le résumé du profil utilisateur.
     */
    AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request);

}