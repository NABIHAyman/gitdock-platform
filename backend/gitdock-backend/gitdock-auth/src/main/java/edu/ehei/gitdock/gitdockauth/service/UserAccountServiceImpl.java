package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.dto.CreateUserRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockauth.dto.UserSummaryDTO;
import edu.ehei.gitdock.gitdockauth.enums.RoleName;
import edu.ehei.gitdock.gitdockauth.exception.UserNotFoundException;
import edu.ehei.gitdock.gitdockauth.model.*;
import edu.ehei.gitdock.gitdockauth.repository.*;
import edu.ehei.gitdock.gitdockauth.service.interfaces.IUserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public UserSummaryDTO getUserById(Long id) {
        UserAccount user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable avec id: " + id));
        return mapToSummary(user);
    }

    @Override
    public List<UserSummaryDTO> getUsersSummaries(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findAllById(userIds).stream()
                .filter(u -> !u.isDeleted())
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public UserSummaryDTO getUserByEmail(String email) {
        UserAccount user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("Aucun utilisateur actif trouvé avec cet email"));
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

        Optional<UserAccount> existingUserOpt = userRepository.findByEmailAndIsDeletedFalse(request.getEmail());

        if (existingUserOpt.isPresent()) {
            UserAccount targetUser = existingUserOpt.get();

            boolean hasAccessToCompany = targetUser.getUserRoles().stream()
                    .filter(ur -> ur.getCompany() != null)
                    .anyMatch(ur -> request.getCompanyId() != null
                            && ur.getCompany().getId().equals(request.getCompanyId()));

            if (!hasAccessToCompany && hostingCompany != null) {
                Role defaultRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER)
                        .orElseThrow(() -> new RuntimeException("Role introuvable"));
                targetUser.addRole(defaultRole, hostingCompany);
                userRepository.save(targetUser);
                log.info("Accès entreprise ajouté pour l'utilisateur existant {}", targetUser.getEmail());
            }
            return mapToSummary(targetUser);
        }

        Company personalCompany = Company.builder()
                .name(request.getFirstName() + "'s Workspace")
                .isPersonal(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        personalCompany = companyRepository.save(personalCompany);

        UserAccount newUser = UserAccount.builder()
                .email(request.getEmail())
                .username(request.getEmail())
                .firstName(request.getFirstName() != null ? request.getFirstName() : "Collaborateur")
                .lastName(request.getLastName() != null ? request.getLastName() : "")
                .password(UUID.randomUUID().toString())
                .company(personalCompany)
                .isEnabled(false)
                .isDeleted(false)
                .build();

        Role ownerRole = roleRepository.findByName(RoleName.ROLE_WORKSPACE_OWNER)
                .orElseThrow(() -> new RuntimeException("Role Workspace Owner introuvable"));
        newUser.addRole(ownerRole, personalCompany);

        if (hostingCompany != null) {
            Role guestRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER)
                    .orElseThrow(() -> new RuntimeException("Role Developer introuvable"));
            newUser.addRole(guestRole, hostingCompany);
        }

        userRepository.save(newUser);

        String tokenString = UUID.randomUUID().toString();
        InvitationToken token = InvitationToken.builder()
                .token(tokenString)
                .user(newUser)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        invitationTokenRepository.save(token);

        String fullLink = invitationUrl + "?token=" + tokenString;

        Map<String, String> payload = Map.of(
                "firstName", newUser.getFirstName() != null ? newUser.getFirstName() : "",
                "lastName", newUser.getLastName() != null ? newUser.getLastName() : "",
                "invitationLink", fullLink,
                "projectName", request.getProjectName() != null ? request.getProjectName() : "un projet"
        );

        NotificationEventDTO event = NotificationEventDTO.builder()
                .targetUserId(newUser.getId())
                .targetEmail(newUser.getEmail())
                .type("TYPE_PROJECT_INVITATION")
                .payload(payload)
                .build();

        rabbitTemplate.convertAndSend("gitdock.exchange", "notification.routing.key", event);

        log.info("Événement PROJECT_INVITATION envoyé à RabbitMQ pour {}", newUser.getEmail());

        return mapToSummary(newUser);
    }

    @Override
    @Transactional
    public UserSummaryDTO updateUser(Long id, CreateUserRequestDTO request) {
        checkPermissionOnUser(id);

        UserAccount userToUpdate = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        if (request.getFirstName() != null) {
            userToUpdate.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            userToUpdate.setLastName(request.getLastName());
        }

        if (request.getRole() != null) {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserAccount currentUser = userRepository.findByEmailAndIsDeletedFalse(currentUserEmail).orElseThrow();
            Company contextCompany = currentUser.getCompany();

            userToUpdate.getUserRoles().stream()
                    .filter(ur -> ur.getCompany() != null && ur.getCompany().getId().equals(contextCompany.getId()))
                    .findFirst()
                    .ifPresent(userRole -> {
                        Role newRole = roleRepository.findByName(RoleName.valueOf(request.getRole()))
                                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));
                        userRole.setRole(newRole);
                    });
        }

        userRepository.save(userToUpdate);
        return mapToSummary(userToUpdate);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        checkPermissionOnUser(id);

        UserAccount user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        user.setEnabled(false);
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {
        checkPermissionOnUser(id);

        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        user.setEnabled(true);
        user.setDeleted(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        checkPermissionOnUser(id);

        UserAccount user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable avec id: " + id));

        if (user.getEmail().equals(GHOST_EMAIL)) {
            throw new RuntimeException("Impossible de supprimer l'utilisateur système (Ghost).");
        }

        invitationTokenRepository.deleteByUserId(id);
        activationTokenRepository.deleteByUser_Id(id);
        passwordResetTokenRepository.deleteByUserId(id);

        userRepository.delete(user);

        rabbitTemplate.convertAndSend("user.exchange", "user.deleted", id);
    }

    @Override
    public List<UserSummaryDTO> getAllUsersSummaries() {
        return userRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSummaryDTO> getUsersByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findByEmailInAndIsDeletedFalse(emails).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        log.info("Vérification en base Auth pour l'ID : {}", id);
        return userRepository.existsById(id);
    }

    private UserSummaryDTO mapToSummary(UserAccount user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isEnabled(user.isEnabled())
                .build();
    }

    private void checkPermissionOnUser(Long targetUserId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount currentUser = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserAccount targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        boolean isSuperAdmin = currentUser.getUserRoles().stream()
                .anyMatch(r -> r.getRole().getName() == RoleName.ROLE_SUPER_ADMIN);
        if (isSuperAdmin) {
            return;
        }

        Long currentCompanyId = currentUser.getCompany().getId();
        Long targetCompanyId = targetUser.getCompany() != null ? targetUser.getCompany().getId() : -1L;

        boolean isInMyScope = targetCompanyId.equals(currentCompanyId)
                || targetUser.getUserRoles().stream()
                .anyMatch(ur -> ur.getCompany() != null && ur.getCompany().getId().equals(currentCompanyId));

        if (!isInMyScope) {
            throw new RuntimeException("ACCÈS REFUSÉ : Vous ne pouvez pas gérer cet utilisateur.");
        }

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("Vous ne pouvez pas vous désactiver vous-même.");
        }
    }
}
