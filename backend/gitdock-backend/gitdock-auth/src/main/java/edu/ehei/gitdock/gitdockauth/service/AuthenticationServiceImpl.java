package edu.ehei.gitdock.gitdockauth.service.impl;

import edu.ehei.gitdock.gitdockauth.dto.AuthenticationRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.AuthenticationResponseDTO;
import edu.ehei.gitdock.gitdockauth.dto.RegisterRequestDTO;
import edu.ehei.gitdock.gitdockauth.enums.SubscriptionPlan;
import edu.ehei.gitdock.gitdockauth.model.Company;
import edu.ehei.gitdock.gitdockauth.model.Role;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.enums.RoleName;
import edu.ehei.gitdock.gitdockauth.repository.RoleRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import edu.ehei.gitdock.gitdockauth.repository.CompanyRepository;
import edu.ehei.gitdock.gitdockauth.security.JwtService;
import edu.ehei.gitdock.gitdockauth.service.interfaces.AuthenticationService;
import edu.ehei.gitdock.gitdockauth.service.interfaces.UserActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service d'authentification.
 * Gère l'inscription des utilisateurs, la création de leur environnement par défaut,
 * l'authentification (Login) et la génération des réponses JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserActivationService userActivationService;

    /**
     * Inscrit un nouvel utilisateur dans le système.
     * Cette méthode est transactionnelle : si une étape échoue, tout est annulé.
     *
     * @param request DTO contenant les informations d'inscription (nom, email, etc.)
     */
    @Override
    @Transactional
    public void register(RegisterRequestDTO request) {

        // Vérification de l'unicité de l'email
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }

        // Gestion du nom d'utilisateur : utilise celui fourni ou extrait la partie avant '@' de l'email
        String usernameToSave = request.getUsername();
        if (usernameToSave == null || usernameToSave.isBlank()) {
            usernameToSave = request.getEmail().split("@")[0];
        }

        // Génération d'un mot de passe temporaire aléatoire.
        // L'utilisateur ne définit pas son mot de passe ici, mais lors de l'activation du compte par email.
        String temporaryPassword = java.util.UUID.randomUUID().toString();

        // Construction de l'objet utilisateur (désactivé par défaut)
        var user = UserAccount.builder()
                .username(usernameToSave)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .isEnabled(false) // Le compte reste inactif jusqu'à la validation du token par email
                .isDeleted(false)
                .build();

        // 1. Sauvegarde de l'utilisateur en base pour générer son ID
        UserAccount savedUser = userAccountRepository.save(user);

        // 2. Création automatique d'une "Company" personnelle (Workspace par défaut) pour l'utilisateur
        var personalCompany = Company.builder()
                .name(request.getFirstName() + "'s Workspace")
                .isPersonal(true)
                .subscriptionPlan(SubscriptionPlan.FREE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        Company savedCompany = companyRepository.save(personalCompany);

        // 3. Récupération du rôle "Propriétaire de Workspace" et assignation à l'utilisateur
        Role ownerRole = roleRepository.findByName(RoleName.ROLE_WORKSPACE_OWNER)
                .orElseThrow(() -> new RuntimeException("Erreur critique: Rôle WORKSPACE_OWNER non initialisé."));

        // Liaison de l'utilisateur à son entreprise personnelle avec le rôle approprié
        savedUser.setCompany(savedCompany);
        savedUser.addRole(ownerRole, savedCompany);

        userAccountRepository.save(savedUser);

        // 4. Déclenchement du processus d'envoi d'email d'activation
        userActivationService.sendActivationEmail(savedUser);
    }

    /**
     * Authentifie un utilisateur via Spring Security et génère les tokens JWT.
     *
     * @param request DTO contenant l'email et le mot de passe.
     * @return AuthenticationResponseDTO contenant les tokens et les infos de l'utilisateur.
     */
    @Override
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        // Délègue la vérification des identifiants au gestionnaire d'authentification de Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Récupération de l'utilisateur complet depuis la base de données
        var user = userAccountRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Construction de la réponse détaillée
        return buildAuthResponse(user);
    }

    /**
     * Construit la réponse d'authentification contenant les tokens et le résumé du profil.
     *
     * @param user L'entité utilisateur authentifiée.
     * @return DTO complet pour le frontend.
     */
    private AuthenticationResponseDTO buildAuthResponse(UserAccount user) {
        // Génération des tokens JWT (Access & Refresh)
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Construction de la liste des workspaces auxquels l'utilisateur a accès via ses rôles
        List<AuthenticationResponseDTO.WorkspaceSummaryDTO> workspaces = user.getUserRoles().stream()
                .filter(ur -> ur.getCompany() != null)
                .map(ur -> AuthenticationResponseDTO.WorkspaceSummaryDTO.builder()
                        .companyId(ur.getCompany().getId())
                        .companyName(ur.getCompany().getName())
                        .isPersonal(ur.getCompany().isPersonal())
                        .role(ur.getRole().getName().name())
                        .build())
                .collect(Collectors.toList());

        // Récupération de tous les rôles de l'utilisateur sous forme de liste de chaînes
        List<String> myRoles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .toList();

        // Logique de hiérarchie pour déterminer le "Rôle Principal" à afficher dans l'interface.
        // On vérifie du plus privilégié au moins privilégié.
        String mainRole;
        if (myRoles.contains(RoleName.ROLE_SUPER_ADMIN.name())) {
            mainRole = RoleName.ROLE_SUPER_ADMIN.name();
        } else if (myRoles.contains(RoleName.ROLE_COMPANY_ADMIN.name())) {
            mainRole = RoleName.ROLE_COMPANY_ADMIN.name();
        } else if (myRoles.contains(RoleName.ROLE_MANAGER.name())) {
            mainRole = RoleName.ROLE_MANAGER.name();
        } else if (myRoles.contains(RoleName.ROLE_DEVELOPER.name())) {
            mainRole = RoleName.ROLE_DEVELOPER.name();
        } else if (myRoles.contains(RoleName.ROLE_TESTER.name())) {
            mainRole = RoleName.ROLE_TESTER.name();
        } else {
            mainRole = RoleName.ROLE_WORKSPACE_OWNER.name();
        }

        // Création du résumé utilisateur (UserSummaryDTO)
        AuthenticationResponseDTO.UserSummaryDTO userSummary = AuthenticationResponseDTO.UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .role(mainRole)
                .workspaces(workspaces)
                .build();

        // Retourne la réponse finale avec tokens et données utilisateur
        return AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userSummary)
                .build();
    }
}