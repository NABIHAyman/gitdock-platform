package edu.ehei.gitdock.gitdocksync.strategy;

import edu.ehei.gitdock.gitdocksync.dto.SyncResultDTO;

/**
 * Interface contrat pour implémenter une stratégie de synchronisation
 * pour une plateforme Git (GitHub, GitLab, Bitbucket, etc.)
 */
public interface IGitPlatformSyncStrategy {

    /**
     * Détermine si cette stratégie gère la plateforme donnée.
     * @param platform Ex: "GITHUB", "GITLAB", "BITBUCKET"
     * @return true si cette stratégie peut gérer cette plateforme
     */
    boolean supports(String platform);

    /**
     * Récupère TOUTES les données du projet (branches, commits).
     * @param repoUrl L'URL du repository (ex: https://github.com/owner/repo)
     * @param userId L'ID de l'utilisateur GitDock qui effectue la synchro
     * @return Un objet SyncResultDTO normalisé avec toutes les données
     * @throws Exception Si l'aspiration échoue
     */
    SyncResultDTO fetchProjectData(String repoUrl, Long userId) throws Exception;

    /**
     * Extrait les parties owner/repo d'une URL.
     * @param repoUrl Ex: "https://github.com/owner/repo.git"
     * @return Un tableau [owner, repo]
     */
    String[] extractRepoInfo(String repoUrl) throws IllegalArgumentException;

    /**
     * Récupère le nom de la plateforme supportée (Utilisé pour le logging).
     */
    default String getPlatformName() {
        return this.getClass().getSimpleName()
                .replace("SyncStrategyImpl", "")
                .toUpperCase();
    }
}