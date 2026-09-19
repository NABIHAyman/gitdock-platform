package edu.ehei.gitdock.gitdockauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Objet de transfert de données (DTO) renvoyé après une authentification réussie.
 * <p>
 * Ce DTO contient tout ce dont le frontend a besoin pour démarrer la session :
 * 1. Les jetons de sécurité (Access & Refresh Tokens).
 * 2. Le profil résumé de l'utilisateur.
 * 3. La liste des espaces de travail auxquels il a accès.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponseDTO {

    /**
     * Le jeton d'accès JWT (courte durée, ex: 15min-1h).
     * Doit être envoyé dans le header 'Authorization: Bearer ...' de chaque requête API.
     */
    private String accessToken;

    /**
     * Le jeton de rafraîchissement (longue durée, ex: 7 jours).
     * Permet d'obtenir un nouveau accessToken sans obliger l'utilisateur à se reconnecter.
     */
    private String refreshToken;

    /**
     * Les informations de l'utilisateur connecté.
     */
    private UserSummaryDTO user;

    /**
     * Classe interne statique contenant les détails essentiels de l'utilisateur
     * pour l'affichage dans l'interface utilisateur (UI).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDTO {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String avatarUrl;

        /**
         * L'ID de l'entreprise (Workspace) actuellement active ou par défaut.
         */
        private Long companyId;

        /**
         * Le rôle principal de l'utilisateur (ex: SUPER_ADMIN, MANAGER).
         * Utilisé par le frontend pour masquer/afficher certains menus.
         */
        private String role;

        /**
         * Liste de tous les environnements de travail (Entreprises) accessibles par cet utilisateur.
         */
        private List<WorkspaceSummaryDTO> workspaces;
    }

    /**
     * Résumé d'un espace de travail (Company).
     * Permet au frontend de proposer un sélecteur de compte/entreprise (Switch Workspace).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspaceSummaryDTO {
        private Long companyId;
        private String companyName;

        /**
         * Indique s'il s'agit de l'espace personnel de l'utilisateur (créé à l'inscription)
         * ou d'une organisation rejointe.
         */
        private boolean isPersonal;

        /**
         * Le rôle spécifique de l'utilisateur dans CET espace de travail.
         */
        private String role;
    }
}