package edu.ehei.gitdock.gitdockauth.repository;

import edu.ehei.gitdock.gitdockauth.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface d'accès aux données pour l'entité Company (Espace de travail).
 * <p>
 * Elle permet de gérer les opérations CRUD (Create, Read, Update, Delete) sur les entreprises
 * ou les workspaces personnels. Elle étend JpaRepository pour bénéficier des méthodes
 * standard sans avoir à écrire de SQL.
 * </p>
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Recherche une entreprise par son nom exact.
     * <p>
     * Cette méthode est utilisée pour vérifier l'unicité du nom d'un workspace
     * lors de sa création ou pour retrouver un espace spécifique.
     * </p>
     *
     * @param name Le nom de l'entreprise à rechercher.
     * @return Un Optional contenant l'entreprise si elle existe, vide sinon.
     */
    Optional<Company> findByName(String name);
}