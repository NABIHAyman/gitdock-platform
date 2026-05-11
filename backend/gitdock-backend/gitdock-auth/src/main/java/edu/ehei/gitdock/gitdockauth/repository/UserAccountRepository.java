package edu.ehei.gitdock.gitdockauth.repository;


import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;

import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface d'accès aux données pour l'entité UserAccount.
 * <p>
 * Ce repository implémente le pattern "Soft Delete" (Suppression Logique).
 * Au lieu de supprimer physiquement les enregistrements (DELETE SQL), on change
 * un flag (isDeleted) à true. Par conséquent, toutes les méthodes de recherche
 * doivent filtrer explicitement avec "isDeleted = false" pour ne remonter que
 * les utilisateurs actifs.
 * </p>
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Recherche un utilisateur actif par son email.
     * <p>
     * C'est la méthode principale utilisée par le UserDetailsService de Spring Security
     * lors de la connexion (Login). Si un utilisateur est marqué comme supprimé,
     * il ne pourra pas se connecter, même si ses identifiants sont corrects.
     * </p>
     *
     * @param email L'email de l'utilisateur.
     * @return Un Optional contenant l'utilisateur s'il existe et n'est pas supprimé.
     */
    Optional<UserAccount> findByEmailAndIsDeletedFalse(String email);

    /**
     * Vérifie si un email existe déjà en base (y compris parmi les utilisateurs supprimés ou non).
     * <p>
     * Utilisée lors de l'inscription pour empêcher la création de doublons.
     * </p>
     *
     * @param email L'email à vérifier.
     * @return true si l'email est déjà pris.
     */
    boolean existsByEmail(String email);

    /**
     * Récupère la liste de tous les utilisateurs actifs du système.
     * (Souvent utilisé pour les tableaux de bord d'administration).
     */
    List<UserAccount> findAllByIsDeletedFalse();

    /**
     * Récupère tous les utilisateurs actifs appartenant à une entreprise spécifique.
     * <p>
     * Essentiel pour le cloisonnement des données (Multi-tenancy) : on ne veut voir
     * que les collègues de son propre espace de travail.
     * </p>
     *
     * @param companyId L'ID de l'entreprise/workspace.
     * @return La liste des collaborateurs actifs.
     */
    List<UserAccount> findByCompanyIdAndIsDeletedFalse(Long companyId);

    /**
     * Recherche un utilisateur par son ID en s'assurant qu'il n'est pas supprimé.
     * <p>
     * Utilisation d'une requête JPQL explicite (@Query) pour garantir la condition
     * de non-suppression, car le findById standard de JpaRepository ne filtre pas
     * le flag isDeleted par défaut.
     * </p>
     *
     * @param id L'identifiant de l'utilisateur.
     * @return Un Optional sécurisé.
     */
    @Query("SELECT u FROM UserAccount u WHERE u.id = :id AND u.isDeleted = false")
    Optional<UserAccount> findByIdAndIsDeletedFalse(@Param("id") Long id);

    /**
     * Méthode utilitaire par défaut (Java 8+) pour simplifier le code appelant.
     * Elle redirige vers la version sécurisée "AndIsDeletedFalse", évitant aux services
     * d'avoir à connaître le nom exact de la méthode longue.
     */
    default Optional<UserAccount> findByEmail(String email) {
        return findByEmailAndIsDeletedFalse(email);
    }


    UserSummaryDTO getUserByEmail(String email);

    List<UserAccount> findByEmailInAndIsDeletedFalse(List<String> emails);

    // Dans UserAccountRepository.java
    long countByIsDeletedFalse();
// Page<UserAccount> findAllByIsDeletedFalse(Pageable pageable); // Si tu veux de la pagination



}