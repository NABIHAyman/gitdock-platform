package edu.ehei.gitdock.gitdockauth.service.interfaces;

import edu.ehei.gitdock.gitdockauth.dto.CreateUserRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;

import java.util.List;

public interface IUserAccountService {

    List<UserSummaryDTO> getUsersSummaries(List<Long> userIds);

    UserSummaryDTO getUserByEmail(String email);

    UserSummaryDTO getUserById(Long id);

    UserSummaryDTO inviteUserFromProject(InviteCollaboratorRequestDTO request);

    UserSummaryDTO updateUser(Long id, CreateUserRequestDTO request);

    void softDeleteUser(Long id);

    void restoreUser(Long id);

    void deleteUser(Long id);

    List<UserSummaryDTO> getUsersByEmails(List<String> emails);

    List<UserSummaryDTO> getAllUsersSummaries();
}