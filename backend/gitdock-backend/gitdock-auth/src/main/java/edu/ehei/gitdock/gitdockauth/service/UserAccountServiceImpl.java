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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountServiceImpl implements IUserAccountService {

    private final UserAccountRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final RabbitTemplate rabbitTemplate;

    // Ajout des repository manquants pour le nettoyage
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private static final String GHOST_EMAIL = "ghost@gitdock.system";

    @Value("${application.mail.invitation-url:http://localhost:5173/accept-invitation}")
    private String invitationUrl;

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

        // --- RÈGLE MÉTIER RESTAURÉE : SCÉNARIO A (L'utilisateur existe déjà) ---
        Optional<UserAccount> existingUserOpt = userRepository.findByEmailAndIsDeletedFalse(request.getEmail());

        if (existingUserOpt.isPresent()) {
            UserAccount targetUser = existingUserOpt.get();

            // Vérifie si l'utilisateur est déjà dans l'entreprise
            boolean hasAccessToCompany = targetUser.getUserRoles().stream()
                    .filter(ur -> ur.getCompany() != null)
                    .anyMatch(ur -> ur.getCompany().getId().equals(request.getCompanyId()));

            if (!hasAccessToCompany && hostingCompany != null) {
                Role defaultRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER)
                        .orElseThrow(() -> new RuntimeException("Role introuvable"));
                targetUser.addRole(defaultRole, hostingCompany);
                userRepository.save(targetUser);
                log.info("Accès entreprise ajouté pour l'utilisateur existant {}", targetUser.getEmail());
            }
            return mapToSummary(targetUser);
        }

        // --- RÈGLE MÉTIER RESTAURÉE : SCÉNARIO B (Nouvel utilisateur avec Workspace) ---

        // 1. Création de son espace personnel (Workspace)
        Company personalCompany = Company.builder()
                .name(request.getFirstName() + "'s Workspace")
                .isPersonal(true)
                // .subscriptionPlan(SubscriptionPlan.FREE) // Si tu as gardé l'Enum SubscriptionPlan
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        personalCompany = companyRepository.save(personalCompany);

        // 2. Création du compte
        UserAccount newUser = UserAccount.builder()
                .email(request.getEmail())
                .username(request.getEmail())
                .firstName(request.getFirstName() != null ? request.getFirstName() : "Collaborateur")
                .lastName(request.getLastName() != null ? request.getLastName() : "")
                .password(UUID.randomUUID().toString())
                .company(personalCompany) // Son entreprise principale est son workspace
                .isEnabled(false)
                .isDeleted(false)
                .build();

        // 3. Assignation des rôles
        Role ownerRole = roleRepository.findByName(RoleName.ROLE_WORKSPACE_OWNER)
                .orElseThrow(() -> new RuntimeException("Role Workspace Owner introuvable"));
        newUser.addRole(ownerRole, personalCompany);

        if (hostingCompany != null) {
            Role guestRole = roleRepository.findByName(RoleName.ROLE_DEVELOPER)
                    .orElseThrow(() -> new RuntimeException("Role Developer introuvable"));
            newUser.addRole(guestRole, hostingCompany);
        }

        userRepository.save(newUser);

        // 4. Token d'invitation et Email
        String tokenString = UUID.randomUUID().toString();
        InvitationToken token = InvitationToken.builder()
                .token(tokenString)
                .user(newUser)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        invitationTokenRepository.save(token);

        String fullLink = invitationUrl + "?token=" + tokenString;
        /*
        emailSenderService.sendInvitationEmail(
                newUser.getEmail(), newUser.getFirstName(), newUser.getLastName(), fullLink, request.getProjectName()
        );
         */

        java.util.Map<String, String> payload = java.util.Map.of(
                "firstName", newUser.getFirstName() != null ? newUser.getFirstName() : "",
                "lastName", newUser.getLastName() != null ? newUser.getLastName() : "",
                "invitationLink", fullLink,
                "projectName", request.getProjectName() != null ? request.getProjectName() : "un projet"
        );

        edu.ehei.gitdock.gitdockauth.dto.NotificationEventDTO event = edu.ehei.gitdock.gitdockauth.dto.NotificationEventDTO.builder()
                .targetUserId(newUser.getId())
                .targetEmail(newUser.getEmail())
                .type("TYPE_PROJECT_INVITATION")
                .payload(payload)
                .build();

        // Envoi au centre de tri avec l'adresse "notification.routing.key"
        rabbitTemplate.convertAndSend("gitdock.exchange", "notification.routing.key", event);

        log.info("📢 Événement PROJECT_INVITATION envoyé à RabbitMQ pour {}", newUser.getEmail());

        return mapToSummary(newUser);
    }

    @Override
    @Transactional
    public UserSummaryDTO updateUser(Long id, CreateUserRequestDTO request) {
        checkPermissionOnUser(id); // 👈 RÈGLE MÉTIER RESTAURÉE (SÉCURITÉ)

        UserAccount userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        if (request.getFirstName() != null) userToUpdate.setFirstName(request.getFirstName());
        if (request.getLastName() != null) userToUpdate.setLastName(request.getLastName());

        if (request.getRole() != null) {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserAccount currentUser = userRepository.findByEmailAndIsDeletedFalse(currentUserEmail).orElseThrow();
            Company contextCompany = currentUser.getCompany();

            userToUpdate.getUserRoles().stream()
                    .filter(ur -> ur.getCompany().getId().equals(contextCompany.getId()))
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
        checkPermissionOnUser(id); // 👈 SÉCURITÉ
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {
        checkPermissionOnUser(id); // 👈 SÉCURITÉ
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable avec id: " + id));

        // 👈 RÈGLE MÉTIER RESTAURÉE : Protection du Ghost User
        if (user.getEmail().equals(GHOST_EMAIL)) {
            throw new RuntimeException("Impossible de supprimer l'utilisateur système (Ghost).");
        }

        // Nettoyage local propre
        invitationTokenRepository.deleteByUserId(id);
        activationTokenRepository.deleteByUser_Id(id);
        passwordResetTokenRepository.deleteByUserId(id);

        userRepository.delete(user);

        // 📢 Déclenche le protocole fantôme dans gitdock-project !
        rabbitTemplate.convertAndSend("user.exchange", "user.deleted", id);
    }

    @Override
    public List<UserSummaryDTO> getAllUsersSummaries() {
        // On utilise findAllByIsDeletedFalse() pour ne pas remonter les comptes supprimés !
        return userRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    // --- MÉTHODES UTILITAIRES ---

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
        if (isSuperAdmin) return;

        Long currentCompanyId = currentUser.getCompany().getId();
        Long targetCompanyId = targetUser.getCompany() != null ? targetUser.getCompany().getId() : -1L;

        boolean isInMyScope = targetCompanyId.equals(currentCompanyId) ||
                targetUser.getUserRoles().stream().anyMatch(ur -> ur.getCompany().getId().equals(currentCompanyId));

        if (!isInMyScope) {
            throw new RuntimeException("ACCÈS REFUSÉ : Vous ne pouvez pas gérer cet utilisateur.");
        }

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("Vous ne pouvez pas vous désactiver vous-même.");
        }
    }

    @Override
    public List<UserSummaryDTO> getUsersByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) return new ArrayList<>();

        return userRepository.findByEmailInAndIsDeletedFalse(emails).stream()
                .map(user -> UserSummaryDTO.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .isEnabled(user.isEnabled())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        log.info("Vérification en base Auth pour l'ID : {}", id);
        return userRepository.existsById(id);
    }
}