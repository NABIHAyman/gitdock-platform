package edu.ehei.gitdock.gitdockauth.repository;

import edu.ehei.gitdock.gitdockauth.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockauth.model.AccessToken;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByUserAndPlatform(UserAccount user, ProjectPlatform platform);
    boolean existsByUserAndPlatform(UserAccount user, ProjectPlatform platform);
}