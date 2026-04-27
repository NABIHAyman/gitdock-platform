package edu.ehei.gitdock.gitdockproject.enums;

/**
 * Définit les rôles applicables au contexte STRICT d'un projet.
 * (On ignore ici les rôles globaux comme SUPER_ADMIN ou COMPANY_ADMIN
 * qui sont gérés exclusivement par gitdock-auth).
 */
public enum ProjectRole {
    MANAGER,
    DEVELOPER,
    TESTER,
    GUEST // Si tu en as besoin plus tard
}