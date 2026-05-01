package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.dto.CreateUserRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;
import edu.ehei.gitdock.gitdockauth.exception.UserNotFoundException;
import edu.ehei.gitdock.gitdockauth.model.*;
import edu.ehei.gitdock.gitdockauth.repository.*;
import edu.ehei.gitdock.gitdockauth.service.interfaces.IUserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.ehei.gitdock.gitdockauth.enums.RoleName;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountServiceImpl implements IUserAccountService {

    private final UserAccountRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private static final String GHOST_EMAIL = "ghost@gitdock.system";

    @Value("${application.mail.invitation-url:http://localhost:5173/accept-invitation}")
    private String invitationUrl;

    // =========================
    // GET BY ID (FIX IMPORTANT)
    // =========================
    @Override
    public UserSummaryDTO getUserById(Long id) {
        UserAccount user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable avec id: " + id));

        return mapToSummary(user);
    }

    // =========================
    // OTHER METHODS
    // =========================

    @Override
    public List<UserSummaryDTO> getUsersSummaries(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return new ArrayList<>();

        return userRepository.findAllById(userIds).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public UserSummaryDTO getUserByEmail(String email) {
        UserAccount user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));
        return mapToSummary(user);
    }

    @Override
    @Transactional
    public UserSummaryDTO inviteUserFromProject(InviteCollaboratorRequestDTO request) {

        Company hostingCompany = null;
        if (request.getCompanyId() != null) {
            hostingCompany = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Entreprise introuvable"));
        }

        Optional<UserAccount> existingUserOpt =
                userRepository.findByEmailAndIsDeletedFalse(request.getEmail());

        if (existingUserOpt.isPresent()) {
            UserAccount user = existingUserOpt.get();
            return mapToSummary(user);
        }

        Company personalCompany = Company.builder()
                .name(request.getFirstName() + "'s Workspace")
                .isPersonal(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        companyRepository.save(personalCompany);

        UserAccount newUser = UserAccount.builder()
                .email(request.getEmail())
                .username(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(UUID.randomUUID().toString())
                .company(personalCompany)
                .isEnabled(false)
                .isDeleted(false)
                .build();

        Role ownerRole = roleRepository.findByName(RoleName.ROLE_WORKSPACE_OWNER)
                .orElseThrow(() -> new RuntimeException("Role introuvable"));

        newUser.addRole(ownerRole, personalCompany);

        userRepository.save(newUser);

        String tokenString = UUID.randomUUID().toString();

        InvitationToken token = InvitationToken.builder()
                .token(tokenString)
                .user(newUser)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        invitationTokenRepository.save(token);

        String link = invitationUrl + "?token=" + tokenString;

        rabbitTemplate.convertAndSend(
                "gitdock.exchange",
                "notification.routing.key",
                newUser.getEmail()
        );

        return mapToSummary(newUser);
    }

    @Override
    @Transactional
    public UserSummaryDTO updateUser(Long id, CreateUserRequestDTO request) {

        UserAccount user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        userRepository.save(user);

        return mapToSummary(user);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {

        UserAccount user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        user.setEnabled(false);
        user.setDeleted(true);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {

        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        user.setEnabled(true);
        user.setDeleted(false);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {

        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        if (user.getEmail().equals(GHOST_EMAIL)) {
            throw new RuntimeException("Impossible de supprimer l'utilisateur système");
        }

        userRepository.delete(user);
    }

    @Override
    public List<UserSummaryDTO> getAllUsersSummaries() {
        return userRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSummaryDTO> getUsersByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) return new ArrayList<>();

        return userRepository.findByEmailInAndIsDeletedFalse(emails).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    // =========================
    // MAPPER
    // =========================
    private UserSummaryDTO mapToSummary(UserAccount user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isEnabled(user.isEnabled())
                .build();
    }
}