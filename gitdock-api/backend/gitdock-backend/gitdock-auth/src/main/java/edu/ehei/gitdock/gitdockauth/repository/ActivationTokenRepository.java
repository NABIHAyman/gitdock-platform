package edu.ehei.gitdock.gitdockauth.repository;

import edu.ehei.gitdock.gitdockauth.model.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Interface d'accès aux données pour l'entité ActivationToken.
 * <p>
 * Elle hérite de JpaRepository pour bénéficier des méthodes standard (save, delete, findById).
 * Les méthodes supplémentaires définies ici utilisent la convention de nommage Spring Data JPA
 * pour générer automatiquement les requêtes SQL.
 * </p>
 */
@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {

    /**
     * Supprime tous les tokens associés à un utilisateur spécifique.
     * <p>
     * Cette méthode est utilisée pour nettoyer les anciens tokens avant d'en générer un nouveau
     * (par exemple lors d'une demande de renvoi d'email), garantissant qu'il n'y a qu'un seul
     * token valide à la fois.
     * </p>
     *
     * @param userId L'identifiant de l'utilisateur.
     */
    void deleteByUser_Id(Long userId);

    /**
     * Recherche un token valide pour l'activation.
     * <p>
     * La requête générée vérifie trois conditions simultanément :
     * 1. Le token correspond à la chaîne fournie (`findByToken`).
     * 2. Le token n'a pas encore été consommé (`UsedFalse`).
     * 3. La date d'expiration est postérieure à la date actuelle (`ExpiresAtAfter`),
     * c'est-à-dire que le token est encore valide dans le temps.
     * </p>
     *
     * @param token Le token à vérifier.
     * @param now   La date/heure actuelle (LocalDateTime.now()) pour comparer l'expiration.
     * @return Un Optional contenant le token s'il est valide, vide sinon.
     */
    Optional<ActivationToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, LocalDateTime now);
}