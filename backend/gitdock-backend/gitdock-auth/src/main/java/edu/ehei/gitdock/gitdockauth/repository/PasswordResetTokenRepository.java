package edu.ehei.gitdock.gitdockauth.repository;

import edu.ehei.gitdock.gitdockauth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository gérant l'accès aux données des tokens de réinitialisation de mot de passe.
 * <p>
 * Cette interface permet de créer, trouver et supprimer les tokens temporaires
 * générés lorsqu'un utilisateur oublie son mot de passe.
 * </p>
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Recherche un token en base à partir de sa chaîne de caractères.
     * <p>
     * Utilisée lors de la phase de confirmation : quand l'utilisateur clique sur le lien,
     * on vérifie si ce token existe bien en base avant de vérifier sa date d'expiration.
     * </p>
     *
     * @param token La chaîne du token (UUID).
     * @return Un Optional contenant l'entité si trouvée.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Supprime tous les tokens associés à un utilisateur spécifique.
     * <p>
     * <b>Note technique :</b> Comme c'est une requête d'écriture (DELETE) personnalisée via @Query,
     * l'annotation @Modifying est obligatoire pour indiquer à Spring Data JPA qu'il ne s'agit pas
     * d'une lecture seule.
     * Cette méthode est utile pour nettoyer les anciennes demandes de reset avant d'en créer une nouvelle.
     * </p>
     *
     * @param userId L'identifiant de l'utilisateur.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}