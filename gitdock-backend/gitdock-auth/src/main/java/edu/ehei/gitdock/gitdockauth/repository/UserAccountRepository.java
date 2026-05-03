package edu.ehei.gitdock.gitdockauth.repository;

import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailAndIsDeletedFalse(String email);

    Optional<UserAccount> findByEmail(String email);

    List<UserAccount> findAllByIsDeletedFalse();

    List<UserAccount> findByEmailInAndIsDeletedFalse(List<String> emails);

    List<UserAccount> findByCompanyIdAndIsDeletedFalse(Long companyId);

    Optional<UserAccount> findByIdAndIsDeletedFalse(Long id);

    long countByIsDeletedFalse();

    // 🔥 AJOUT OBLIGATOIRE (CAUSE DE TON ERREUR)
    boolean existsByEmail(String email);
}