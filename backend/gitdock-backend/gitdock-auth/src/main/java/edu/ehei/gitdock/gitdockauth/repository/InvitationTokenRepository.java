package edu.ehei.gitdock.gitdockauth.repository;

import edu.ehei.gitdock.gitdockauth.dto.ActivateAccountRequestDTO;
import edu.ehei.gitdock.gitdockauth.model.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {
    Optional<InvitationToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM InvitationToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}