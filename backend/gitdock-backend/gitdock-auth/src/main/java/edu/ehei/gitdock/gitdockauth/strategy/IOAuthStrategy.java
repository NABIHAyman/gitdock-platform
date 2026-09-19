package edu.ehei.gitdock.gitdockauth.strategy;

import edu.ehei.gitdock.gitdockauth.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;

public interface IOAuthStrategy {

    // Vérifie si cette stratégie gère cette plateforme (ex: GITHUB)
    boolean supports(ProjectPlatform platform);

    // Génère l'URL pour rediriger l'utilisateur vers la page de consentement
    String getAuthorizationUrl();

    // Échange le code de retour contre un Access Token et le sauvegarde
    void exchangeCodeForToken(String code, UserAccount user);

    // Utilise le Refresh Token pour obtenir un nouvel Access Token
    void refreshAccessToken(UserAccount user);
}