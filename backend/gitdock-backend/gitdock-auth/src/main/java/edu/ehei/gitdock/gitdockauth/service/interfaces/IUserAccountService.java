package edu.ehei.gitdock.gitdockauth.service.interfaces;

import edu.ehei.gitdock.gitdockauth.dto.CreateUserRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IUserAccountService {
    /**
     * Récupère les informations de base de plusieurs utilisateurs à partir de leurs IDs.
     */
    List<UserSummaryDTO> getUsersSummaries(List<Long> userIds);

    UserSummaryDTO getUserByEmail(String email);

    UserSummaryDTO inviteUserFromProject(InviteCollaboratorRequestDTO request);

    UserSummaryDTO updateUser(Long id, CreateUserRequestDTO request);

    void softDeleteUser(Long id);

    void restoreUser(Long id);

    void deleteUser(Long id);

    List<UserSummaryDTO> getUsersByEmails(List<String> emails);

    List<UserSummaryDTO> getAllUsersSummaries();

}