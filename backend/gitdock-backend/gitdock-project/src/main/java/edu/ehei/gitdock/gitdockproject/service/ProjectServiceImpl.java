package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.*;
import edu.ehei.gitdock.gitdockproject.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockproject.enums.ProjectRole;
import edu.ehei.gitdock.gitdockproject.enums.ProjectVisibility;
import edu.ehei.gitdock.gitdockproject.model.*;
import edu.ehei.gitdock.gitdockproject.repository.*;
import edu.ehei.gitdock.gitdockproject.security.JwtService;
import edu.ehei.gitdock.gitdockproject.service.interfaces.IProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import feign.FeignException;
import edu.ehei.gitdock.gitdockproject.dto.InviteCollaboratorRequestDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements IProjectService {

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;

    private final RabbitTemplate rabbitTemplate;

    // Nouveau : Le client Feign pour communiquer avec l'Auth
    private final AuthServiceClient authServiceClient;

    // Nouveau : Pour récupérer le token de la requête courante (pour obtenir le
    // companyId)
    private final HttpServletRequest request;
    private final JwtService jwtService;

    /*
    // On s'injecte nous-mêmes (via le proxy)
    @Autowired
    @Lazy
    private ProjectServiceImpl self;
     */

    @Override
    @Transactional
    public ProjectDTO createProject(CreateProjectRequestDTO requestDto) {

        // 1. Extraction des informations de l'utilisateur depuis le JWT
        Long currentUserId = getCurrentUserId();
        Long companyId = getCurrentCompanyId(); // On suppose qu'il est dans le token, à ajouter si ce n'est pas le cas

        if (companyId == null) {
            throw new RuntimeException(
                    "Impossible de créer un projet : L'utilisateur n'est rattaché à aucune entreprise.");
        }

        boolean isPrivate = ProjectVisibility.valueOf(requestDto.getVisibility().toUpperCase()) == ProjectVisibility.PRIVATE;
        boolean isGithub = ProjectPlatform.valueOf(requestDto.getPlatform().toUpperCase()) == ProjectPlatform.GITHUB;

        if (isPrivate && isGithub) {
            try {
                // On vérifie la présence du token GitHub via Feign
                String token = authServiceClient.getGithubToken("Bearer " + getRawJwt(), currentUserId);
                if (token == null || token.isBlank()) {
                    throw new RuntimeException("OAUTH_REQUIRED");
                }
            } catch (Exception e) {
                // Pas de token ou erreur de communication → on bloque la création
                log.warn("Tentative d'ajout de projet privé sans token GitHub pour user {}", currentUserId);
                throw new RuntimeException("OAUTH_REQUIRED");
            }
        }

        /*
         * ProjectVisibility visibility =
         * ProjectVisibility.valueOf(requestDto.getVisibility().toUpperCase());
         * 
         * // 👇 1. ON RÉCUPÈRE LE PLAN DEPUIS LE JWT
         * String currentPlan = getCurrentSubscriptionPlan();
         * 
         * // 👇 2. LA RÈGLE MÉTIER SAAS EST DE RETOUR ! 🛡️
         * if (visibility == ProjectVisibility.PRIVATE) {
         * if ("FREE".equalsIgnoreCase(currentPlan)) {
         * // Utilise une RuntimeException ou une exception métier personnalisée
         * throw new
         * RuntimeException("Votre plan actuel (Gratuit) ne permet pas de créer des projets privés. Passez au plan PRO."
         * );
         * }
         * }
         */
        // 2. Création du projet
        Project project = Project.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .url(requestDto.getUrl())
                .platform(ProjectPlatform.valueOf(requestDto.getPlatform().toUpperCase()))
                .visibility(ProjectVisibility.valueOf(requestDto.getVisibility().toUpperCase()))
                .companyId(companyId) // Utilisation de l'ID
                .managedById(currentUserId) // Utilisation de l'ID
                .build();

        Project savedProject = projectRepository.save(project);

        UserProject userProject = UserProject.builder()
                .userId(currentUserId)
                .project(savedProject)
                .role(ProjectRole.MANAGER)
                .assignedAt(LocalDateTime.now())
                .assignedById(currentUserId)
                .build();

        userProjectRepository.save(userProject);

        // 4. SYNCHRONISATION EXTERNE (ASYNCHRONE VIA RABBITMQ)
        log.info("Envoi de la requête de synchronisation à RabbitMQ pour le projet {}", savedProject.getName());

        SyncRequestMessageDTO message = SyncRequestMessageDTO.builder()
                .projectId(savedProject.getId())
                .repoUrl(savedProject.getUrl())
                .userId(currentUserId)
                .platform(savedProject.getPlatform().name()) // "GITHUB", "GITLAB", etc.
                .projectName(savedProject.getName())         // Le vrai nom !
                .build();

        // On poste le message dans l'échange et on rend la main immédiatement
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_REQUEST,
                message
        );

        return mapToProjectDto(savedProject);

    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        Set<Project> accessibleProjects = fetchAccessibleProjects();

        // On récupère tous les IDs des managers uniques
        List<Long> managerIds = accessibleProjects.stream()
                .map(Project::getManagedById)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // BATCH FETCHING : On interroge l'Auth Service une seule fois !
        Map<Long, UserSummaryDTO> managerMap = getManagersData(managerIds);

        return accessibleProjects.stream()
                .map(p -> mapToProjectDtoWithManagerData(p, managerMap.get(p.getManagedById())))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CollaboratorGroupedDTO> getCollaboratorsGrouped() {
        Set<Project> visibleProjects = fetchAccessibleProjects();

        // Extraire tous les userIds de tous les projets
        Set<Long> userIdsToFetch = new HashSet<>();
        for (Project p : visibleProjects) {
            for (UserProject up : p.getUserProjects()) {
                userIdsToFetch.add(up.getUserId());
            }
        }

        // BATCH FETCHING des collaborateurs
        Map<Long, UserSummaryDTO> usersMap = getManagersData(new ArrayList<>(userIdsToFetch));

        List<CollaboratorGroupedDTO> grouped = new ArrayList<>();

        for (Project p : visibleProjects) {
            List<CollaboratorGroupedDTO.CollaboratorDTO> collaboratorDTOS = p.getUserProjects().stream()
                    .map(userProject -> {
                        UserSummaryDTO userInfo = usersMap.get(userProject.getUserId());

                        // Vraie vérification du statut
                        String status = "disabled";
                        if (userInfo != null) {
                            status = userInfo.isEnabled() ? "active" : "pending";
                        }

                        return CollaboratorGroupedDTO.CollaboratorDTO.builder()
                                .id(userProject.getUserId())
                                .firstName(userInfo != null ? userInfo.getFirstName() : "Inconnu")
                                .lastName(userInfo != null ? userInfo.getLastName() : "Inconnu")
                                .email(userInfo != null ? userInfo.getEmail() : "inconnu@gitdock.com")
                                .role(userProject.getRole().name())
                                .status(status)
                                .build();
                    })
                    .collect(Collectors.toList());

            grouped.add(CollaboratorGroupedDTO.builder()
                    .projectId(p.getId())
                    .projectName(p.getName())
                    .collaborators(collaboratorDTOS)
                    .build());
        }
        return grouped;
    }

    @Override
    @Transactional
    public void removeCollaborator(Long projectId, Long collaboratorId) {
        Long currentUserId = getCurrentUserId();
        boolean isSuperAdmin = isCurrentUserSuperAdmin();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        boolean isProjectManager = project.getManagedById() != null && project.getManagedById().equals(currentUserId);

        // Note: isWorkspaceOwner nécessite le companyId du current user (via JWT)
        boolean isWorkspaceOwner = project.getCompanyId().equals(getCurrentCompanyId());

        if (!isSuperAdmin && !isProjectManager && !isWorkspaceOwner) {
            throw new RuntimeException("Accès refusé : Vous n'avez pas le droit de retirer des membres de ce projet.");
        }

        if (currentUserId.equals(collaboratorId)) {
            throw new RuntimeException("Vous ne pouvez pas vous retirer vous-même du projet via cette action.");
        }

        userProjectRepository.removeUserFromProject(projectId, collaboratorId);
    }

    @Override
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        verifyProjectAccess(project);
        // On récupère les infos du manager s'il y en a un
        UserSummaryDTO managerInfo = null;
        if (project.getManagedById() != null) {
            Map<Long, UserSummaryDTO> managerMap = getManagersData(List.of(project.getManagedById()));
            managerInfo = managerMap.get(project.getManagedById());
        }

        return mapToProjectDtoWithManagerData(project, managerInfo);
    }

    @Override
    public List<BranchDTO> getProjectBranches(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        verifyProjectAccess(project);

        return branchRepository.findByProjectId(projectId).stream()
                .map(branch -> BranchDTO.builder()
                        .id(branch.getId())
                        .name(branch.getName())
                        .lastUpdated(branch.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommitDTO> getProjectCommits(Long projectId, Long branchId, int page, int size) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        verifyProjectAccess(project);

        Pageable pageable = PageRequest.of(page, size);

        //Page<Commit> commitPage = commitRepository.findByBranchIdOrderByCommittedAtDesc(branchId, pageable);

        Page<Commit> commitPage = commitRepository.findByProjectIdOrderByCommittedAtDesc(projectId, pageable);

        log.info("Nombre de commits trouvés pour branchId {} : {}", branchId, commitPage.getTotalElements());

        return commitPage.map(commit -> CommitDTO.builder()
                .id(commit.getId())
                .hash(commit.getHash())
                .message(commit.getMessage())
                .authorName(commit.getAuthorName())
                .date(commit.getCommittedAt())
                .additions(commit.getAdditions())
                .deletions(commit.getDeletions())
                .build());
    }

    // --- Méthodes privées d'aide ---

    private Set<Project> fetchAccessibleProjects() {
        Long currentUserId = getCurrentUserId();
        Long companyId = getCurrentCompanyId();
        boolean isSuperAdmin = isCurrentUserSuperAdmin();
        boolean canViewAllInMyCompany = isCurrentUserWorkspaceOwner() || isCurrentUserCompanyAdmin();

        Set<Project> accessibleProjects = new HashSet<>();

        if (isSuperAdmin) {
            accessibleProjects.addAll(projectRepository.findAll());
            return accessibleProjects;
        }

        accessibleProjects.addAll(projectRepository.findAssignedProjects(currentUserId));

        if (canViewAllInMyCompany && companyId != null) {
            accessibleProjects.addAll(projectRepository.findByCompanyId(companyId));
        }

        return accessibleProjects;
    }

    private ProjectDTO mapToProjectDto(Project project) {
        return mapToProjectDtoWithManagerData(project, null);
    }

    private ProjectDTO mapToProjectDtoWithManagerData(Project project, UserSummaryDTO managerData) {
        ProjectDTO.ManagerDTO managerDTO = null;

        if (managerData != null) {
            managerDTO = ProjectDTO.ManagerDTO.builder()
                    .id(managerData.getId())
                    .firstName(managerData.getFirstName())
                    .lastName(managerData.getLastName())
                    .email(managerData.getEmail())
                    .build();
        }

        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .url(project.getUrl())
                .platform(project.getPlatform().name().toLowerCase())
                .visibility(project.getVisibility().name())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .manager(managerDTO)
                .build();
    }

    // --- BATCH FETCHING avec Feign et Résilience ---

    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackGetManagersData")
    public Map<Long, UserSummaryDTO> getManagersData(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        // Plus de try/catch manuel ! S'il y a une erreur ou un timeout,
        // Resilience4j l'attrape et bascule sur la méthode fallback.
        String bearerToken = "Bearer " + getRawJwt();
        List<UserSummaryDTO> users = authServiceClient.getUsersSummaries(bearerToken, ids);
        return users.stream().collect(Collectors.toMap(UserSummaryDTO::getId, u -> u));
    }

    // 👇 LA MÉTHODE DE SECOURS (Fallback) 👇
    // La signature doit être EXACTEMENT la même que getManagersData, plus un paramètre Throwable
    public Map<Long, UserSummaryDTO> fallbackGetManagersData(List<Long> ids, Throwable t) {
        log.warn("⚠️ Disjoncteur ouvert (ou erreur Feign) : Impossible de joindre gitdock-auth. Raison : {}", t.getMessage());

        Map<Long, UserSummaryDTO> fallbackMap = new HashMap<>();

        // On crée un faux utilisateur générique pour que le Frontend affiche "Service Indisponible"
        // au lieu de juste "Inconnu" ou de planter.
        for (Long id : ids) {
            fallbackMap.put(id, UserSummaryDTO.builder()
                    .id(id)
                    .firstName("Service")
                    .lastName("Indisponible")
                    .email("offline@gitdock.system")
                    .isEnabled(false)
                    .build());
        }

        return fallbackMap;
    }

    @Override
    @Transactional
    public void addCollaborator(Long projectId, AddCollaboratorDTO request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        String bearerToken = "Bearer " + getRawJwt();
        UserSummaryDTO user = null;

        try {
            // 1. On essaie de trouver le gars
            user = authServiceClient.getUserByEmail(bearerToken, request.getEmail());

            if (user == null) {
                throw new RuntimeException("User not found");
            }
        } catch (RuntimeException e) {
            // 2. Il n'existe pas ! On l'invite et on récupère son compte pré-créé !
            if (e instanceof FeignException.NotFound || "User not found".equals(e.getMessage())) {
                log.info("L'utilisateur {} n'existe pas. Création du compte invité...", request.getEmail());
                user = inviteNewUser(request, project, bearerToken);
            } else {
                throw e; // On relance si c'est une autre erreur métier
            }
        } catch (Exception e) {
            log.error("Erreur de communication avec Auth Service", e);
            throw new RuntimeException("Service d'authentification indisponible. Réessayez plus tard.");
        }

        if (user == null) {
            throw new RuntimeException("Erreur critique : L'utilisateur n'a pu ni être trouvé ni être invité.");
        }

        // 3. À ce stade, le gars existe (soit il était déjà là, soit on vient de le
        // créer)
        // On l'ajoute au projet !
        final Long userId = user.getId();
        boolean alreadyExists = project.getUserProjects().stream()
                .anyMatch(up -> up.getUserId().equals(userId));

        if (alreadyExists) {
            throw new RuntimeException("Cet utilisateur est déjà dans le projet.");
        }

        ProjectRole projectRole;
        try {
            projectRole = ProjectRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            projectRole = ProjectRole.DEVELOPER; // Valeur par défaut si erreur de typographie
        }

        UserProject userProject = UserProject.builder()
                .userId(user.getId())
                .project(project)
                .role(projectRole)
                .assignedById(getCurrentUserId())
                .build();

        userProjectRepository.save(userProject);

        // Si c'est un DEV, on prévient la gamification
        if (projectRole == ProjectRole.DEVELOPER) {
            CollaboratorAddedEventDTO gamifEvent = CollaboratorAddedEventDTO.builder()
                    .userId(user.getId())
                    .projectId(projectId)
                    .role(projectRole.name())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_COLLABORATOR_ADDED,
                    gamifEvent
            );
            log.info("Event collaborator.added envoyé pour userId={}", user.getId());
        }

        // 👇 NOUVEAU : On prévient l'utilisateur qu'il a été ajouté au projet
        // (Notification In-App)
        java.util.Map<String, String> payload = java.util.Map.of(
                "projectName", project.getName(),
                "assignedBy", getCurrentUserId().toString(),
                "role", projectRole.name());

        NotificationEventDTO notifEvent = NotificationEventDTO.builder()
                .targetUserId(user.getId()) // 👈 Ici, user.getId() existe et est parfaitement valide !
                .targetEmail(user.getEmail())
                .type("TYPE_PROJECT_INVITATION")
                .payload(payload)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NOTIFICATION,
                notifEvent);

        log.info("L'utilisateur {} a été rattaché avec succès au projet {}", request.getEmail(), project.getName());
    }

    // On modifie la méthode pour qu'elle RETOURNE le UserSummaryDTO
    private UserSummaryDTO inviteNewUser(AddCollaboratorDTO request, Project project, String token) {
        InviteCollaboratorRequestDTO inviteRequest = new InviteCollaboratorRequestDTO(
                request.getEmail(),
                request.getFirstName() != null ? request.getFirstName() : "Collaborateur", // 👈 Vrai Prénom
                request.getLastName() != null ? request.getLastName() : "Invité", // 👈 Vrai Nom
                project.getName(),
                project.getCompanyId());

        UserSummaryDTO createdUser = authServiceClient.inviteUser(token, inviteRequest);

        /*
         * // 👇 NOUVEAU : On prévient l'utilisateur qu'il a été ajouté au projet
         * (Notification In-App pure)
         * java.util.Map<String, String> payload = java.util.Map.of(
         * "projectName", project.getName()
         * );
         * 
         * NotificationEventDTO notifEvent = NotificationEventDTO.builder()
         * .targetUserId(userId)
         * .type("TYPE_PROJECT_INVITATION")
         * .payload(payload)
         * .build();
         * 
         * rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
         * "notification.routing.key", notifEvent);
         */
        log.info("Invitation envoyée avec succès à {}", request.getEmail());
        return createdUser;
    }

    // --- Extraction depuis le JWT ---

    private String getRawJwt() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Long getCurrentUserId() {
        String jwt = getRawJwt();
        return jwt != null ? jwtService.extractUserId(jwt) : null;
    }

    // Note : Assurez-vous d'ajouter `companyId` aux claims dans JwtService de
    // gitdock-auth !
    private Long getCurrentCompanyId() {
        String jwt = getRawJwt();
        // A adapter selon le nom du claim exact que vous avez mis
        return jwt != null ? jwtService.extractCompanyId(jwt) : null;
    }

    private boolean hasRole(String roleName) {
        String jwt = getRawJwt();
        if (jwt == null)
            return false;
        List<String> roles = jwtService.extractRoles(jwt);
        return roles != null && roles.contains(roleName);
    }

    private boolean isCurrentUserSuperAdmin() {
        return hasRole("ROLE_SUPER_ADMIN");
    }

    private boolean isCurrentUserWorkspaceOwner() {
        return hasRole("ROLE_WORKSPACE_OWNER");
    }

    private boolean isCurrentUserCompanyAdmin() {
        return hasRole("ROLE_COMPANY_ADMIN");
    }

    @Override
    public List<CollaboratorGroupedDTO.CollaboratorDTO> getProjectCollaborators(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Récupère tous les IDs des utilisateurs de ce projet
        List<Long> userIds = project.getUserProjects().stream()
                .map(UserProject::getUserId)
                .collect(Collectors.toList());

        // Va chercher leurs infos (Nom, Prénom, Email) dans gitdock-auth via Feign
        Map<Long, UserSummaryDTO> usersMap = getManagersData(userIds);

        return project.getUserProjects().stream().map(up -> {
            UserSummaryDTO user = usersMap.get(up.getUserId());
            return CollaboratorGroupedDTO.CollaboratorDTO.builder()
                    .id(up.getUserId())
                    .firstName(user != null ? user.getFirstName() : "Inconnu")
                    .lastName(user != null ? user.getLastName() : "")
                    .email(user != null ? user.getEmail() : "inconnu@gitdock.com")
                    .role(up.getRole().name())
                    .status(user != null ? (user.isEnabled() ? "active" : "pending") : "disabled")
                    .build();
        }).collect(Collectors.toList());
    }

    private void verifyProjectAccess(Project project) {
        // 1. Le Super Admin est le seul à n'avoir aucune restriction
        if (isCurrentUserSuperAdmin())
            return;

        Long currentUserId = getCurrentUserId();
        Long currentCompanyId = getCurrentCompanyId();

        if (currentUserId == null) {
            throw new RuntimeException("Accès refusé : Utilisateur non identifié.");
        }

        // 2. Vérification : Est-ce que le projet appartient à la même entreprise que
        // l'utilisateur ?
        // C'est la barrière de sécurité la plus importante (Multi-tenant)
        boolean sameCompany = project.getCompanyId() != null && project.getCompanyId().equals(currentCompanyId);

        // 3. Si c'est un admin de l'entreprise (Workspace Owner ou Company Admin)
        if (sameCompany && (isCurrentUserWorkspaceOwner() || isCurrentUserCompanyAdmin())) {
            return;
        }

        // 4. Si c'est le manager direct du projet (doit quand même être dans la même
        // entreprise)
        if (sameCompany && project.getManagedById() != null && project.getManagedById().equals(currentUserId)) {
            return;
        }

        // 5. Si c'est un collaborateur invité (on vérifie la table de liaison
        // user_project)
        boolean isAssigned = project.getUserProjects().stream()
                .anyMatch(up -> up.getUserId().equals(currentUserId));

        if (isAssigned)
            return;

        // Si on arrive ici, c'est une tentative d'intrusion
        log.warn("Tentative d'accès illégal : User {} a tenté d'accéder au Projet {} de la Company {}",
                currentUserId, project.getId(), project.getCompanyId());
        throw new RuntimeException("Accès interdit : Vous n'avez pas les permissions pour consulter ce projet.");
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Vérification des droits
        boolean canDelete = isCurrentUserSuperAdmin() ||
                (isCurrentUserCompanyAdmin() && project.getCompanyId().equals(getCurrentCompanyId())) ||
                (isCurrentUserWorkspaceOwner() && project.getCompanyId().equals(getCurrentCompanyId())) ||
                (project.getManagedById() != null && project.getManagedById().equals(getCurrentUserId()));

        if (!canDelete) {
            throw new RuntimeException("Accès refusé : Vous n'avez pas le droit de supprimer ce projet.");
        }

        // Hibernate s'occupera de supprimer les entités liées (Commits, Branches,
        // UserProject)
        // si le "cascade = CascadeType.ALL" est bien configuré sur l'entité Project.
        projectRepository.delete(project);
        log.info("Projet supprimé avec succès : {}", project.getName());
    }

    @Override
    @Transactional
    public void assignToGhostUser(Long deletedUserId) {
        // L'ID 0 est notre convention universelle pour le Ghost User en micro-services
        Long GHOST_USER_ID = 0L;

        // 1. Retirer le collaborateur supprimé de tous les projets
        userProjectRepository.deleteByUserId(deletedUserId);

        // 2. Trouver tous les projets gérés par lui, et les donner au Ghost
        List<Project> managedProjects = projectRepository.findByManagedById(deletedUserId);
        for (Project project : managedProjects) {
            project.setManagedById(GHOST_USER_ID);
        }
        projectRepository.saveAll(managedProjects);

        // 3. (Optionnel selon tes entités) : Réassigner les commits au Ghost
        // Si ton entité Commit a un champ "userId", fais une requête JPQL dans
        // CommitRepository :
        // @Query("UPDATE Commit c SET c.userId = :ghostId WHERE c.userId = :deletedId")
        // commitRepository.reassignToGhost(deletedUserId, GHOST_USER_ID);

        log.info("👻 Protocole Fantôme terminé pour l'ex-utilisateur ID {}", deletedUserId);
    }

    private String getCurrentSubscriptionPlan() {
        String jwt = getRawJwt();
        return jwt != null ? jwtService.extractSubscriptionPlan(jwt) : "FREE"; // FREE par défaut si introuvable
    }
}
