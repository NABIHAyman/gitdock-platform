package edu.ehei.gitdock.gitdockauth.repository;

import edu.ehei.gitdock.gitdockauth.model.Role;
import edu.ehei.gitdock.gitdockauth.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface d'accès aux données pour l'entité Role.
 * <p>
 * Elle permet de gérer les rôles (ex: USER, ADMIN, SUPER_ADMIN) stockés en base de données.
 * Ces rôles sont essentiels pour la gestion des autorisations (RBAC - Role Based Access Control)
 * dans Spring Security.
 * </p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Recherche un rôle par son nom (Enum).
     * <p>
     * Cette méthode est utilisée lors de la création d'un utilisateur (inscription)
     * ou lors de l'attribution de nouveaux droits, pour récupérer l'entité Role correspondante
     * à partir de l'énumération RoleName.
     * </p>
     *
     * @param name Le nom du rôle (ex: RoleName.ROLE_USER).
     * @return Un Optional contenant le rôle s'il existe en base, vide sinon.
     */
    Optional<Role> findByName(RoleName name);
}