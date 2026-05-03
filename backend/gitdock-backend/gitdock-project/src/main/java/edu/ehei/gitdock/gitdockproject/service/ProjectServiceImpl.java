package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.*;
import edu.ehei.gitdock.gitdockproject.enums.*;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
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
    private final RabbitTemplate rabbitTemplate;

    // =========================
    // CREATE PROJECT
    // =========================
    @Override
    /*
    // On s'injecte nous-mêmes (via le proxy)
    @Autowired
    @Lazy
    private ProjectServiceImpl self;
     */

    @Transactional
    public ProjectDTO createProject(CreateProjectRequestDTO dto) {
    public ProjectDTO createProject(CreateProjectRequestDTO requestDto) {

        // 1. Extraction des informations de l'utilisateur depuis le JWT
        Long currentUserId = getCurrentUserId();
        Long companyId = getCurrentCompanyId(); // On suppose qu'il est dans le token, à ajouter si ce n'est pas le cas
        Long userId = getCurrentUserId();
        Long companyId = getCurrentCompanyId();

        if (companyId == null) {
            throw new RuntimeException("Company manquante dans le token");
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
                .name(dto.getName())
                .description(dto.getDescription())
                .url(dto.getUrl())
                .platform(ProjectPlatform.valueOf(dto.getPlatform().toUpperCase()))
                .visibility(ProjectVisibility.valueOf(dto.getVisibility().toUpperCase()))
                .companyId(companyId)
                .managedById(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);
        project = projectRepository.save(project);

        UserProject up = UserProject.builder()
                .userId(userId)
                .project(project)
        UserProject userProject = UserProject.builder()
                .userId(currentUserId)
                .project(savedProject)
                .role(ProjectRole.MANAGER)
                .assignedAt(LocalDateTime.now())
                .assignedById(userId)
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

        userProjectRepository.save(up);
        // On poste le message dans l'échange et on rend la main immédiatement
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_REQUEST,
                message
        );

        return mapToProjectDto(savedProject);

        return mapToProjectDto(project, null);
    }

    // =========================
    // GET ALL PROJECTS
    // =========================
    @Override
    public List<ProjectDTO> getAllProjects() {
        Set<Project> accessibleProjects = fetchAccessibleProjects();

        Set<Project> projects = fetchAccessibleProjects();

        List<Long> managerIds = projects.stream()
        // On récupère tous les IDs des managers uniques
        List<Long> managerIds = accessibleProjects.stream()
                .map(Project::getManagedById)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
                .collect(Collectors.toList());

        // BATCH FETCHING : On interroge l'Auth Service une seule fois !
        Map<Long, UserSummaryDTO> managerMap = getManagersData(managerIds);

        return accessibleProjects.stream()
                .map(p -> mapToProjectDtoWithManagerData(p, managerMap.get(p.getManagedById())))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
    }

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

        Map<Long, UserSummaryDTO> managers = getManagersData(managerIds);
                        // Vraie vérification du statut
                        String status = "disabled";
                        if (userInfo != null) {
                            status = userInfo.isEnabled() ? "active" : "pending";
                        }

        return projects.stream()
                .map(p -> mapToProjectDto(p, managers.get(p.getManagedById())))
                .sorted(Comparator.comparing(ProjectDTO::getCreatedAt).reversed())
                .toList();
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

    // =========================
    // REMOVE COLLABORATOR (FIX IMPORTANT)
    // =========================
    @Override
    @Transactional
    public void removeCollaborator(Long projectId, Long collaboratorId) {
        Long currentUserId = getCurrentUserId();
        boolean isSuperAdmin = isCurrentUserSuperAdmin();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        boolean isProjectManager = project.getManagedById() != null && project.getManagedById().equals(currentUserId);
        Long currentUserId = getCurrentUserId();

        boolean isManager = project.getManagedById() != null
                && project.getManagedById().equals(currentUserId);
        // Note: isWorkspaceOwner nécessite le companyId du current user (via JWT)
        boolean isWorkspaceOwner = project.getCompanyId().equals(getCurrentCompanyId());

        if (!isManager) {
            throw new RuntimeException("Accès refusé");
        if (!isSuperAdmin && !isProjectManager && !isWorkspaceOwner) {
            throw new RuntimeException("Accès refusé : Vous n'avez pas le droit de retirer des membres de ce projet.");
        }

        if (currentUserId.equals(collaboratorId)) {
            throw new RuntimeException("Vous ne pouvez pas vous retirer vous-même du projet via cette action.");
        }

        // ✅ CORRECT METHOD
        userProjectRepository.removeUserFromProject(projectId, collaboratorId);

        log.info("Collaborateur {} supprimé du projet {}", collaboratorId, projectId);
    }

    // =========================
    // GET PROJECT BY ID
    // =========================
    @Override
    public ProjectDTO getProjectById(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        return mapToProjectDto(project, null);
        verifyProjectAccess(project);
        // On récupère les infos du manager s'il y en a un
        UserSummaryDTO managerInfo = null;
        if (project.getManagedById() != null) {
            Map<Long, UserSummaryDTO> managerMap = getManagersData(List.of(project.getManagedById()));
            managerInfo = managerMap.get(project.getManagedById());
        }

        return mapToProjectDtoWithManagerData(project, managerInfo);
    }

    // =========================
    // BRANCHES
    // =========================
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
        return branchRepository.findByProjectId(projectId)
                .stream()
                .map(b -> BranchDTO.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .lastUpdated(b.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
                .toList();
    }

    // =========================
    // COMMITS
    // =========================
    @Override
    public Page<CommitDTO> getProjectCommits(Long projectId, Long branchId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return commitRepository.findByProjectIdOrderByCommittedAtDesc(projectId, pageable)
                .map(c -> CommitDTO.builder()
                        .id(c.getId())
                        .hash(c.getHash())
                        .message(c.getMessage())
                        .authorName(c.getAuthorName())
                        .date(c.getCommittedAt())
                        .additions(c.getAdditions())
                        .deletions(c.getDeletions())
                        .build());
    }

    // =========================
    // COLLABORATORS
    // =========================
    @Override
    public List<CollaboratorGroupedDTO.CollaboratorDTO> getProjectCollaborators(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        List<Long> userIds = project.getUserProjects()
                .stream()
                .map(UserProject::getUserId)
                .toList();

        Map<Long, UserSummaryDTO> users = getManagersData(userIds);

        return project.getUserProjects().stream()
                .map(up -> {
                    UserSummaryDTO user = users.get(up.getUserId());

                    return CollaboratorGroupedDTO.CollaboratorDTO.builder()
                            .id(up.getUserId())
                            .firstName(user != null ? user.getFirstName() : "Unknown")
                            .lastName(user != null ? user.getLastName() : "")
                            .email(user != null ? user.getEmail() : "")
                            .role(up.getRole().name())
                            .status(user != null && user.isEnabled() ? "active" : "disabled")
                            .build();
                })
                .toList();
    }

    // =========================
    // ADD COLLABORATOR
    // =========================
    @Override
    @Transactional
    public void addCollaborator(Long projectId, AddCollaboratorDTO dto) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        verifyProjectAccess(project);
        UserSummaryDTO user = authServiceClient.getUserByEmail(
                "Bearer " + getRawJwt(),
                dto.getEmail()
        );

        Pageable pageable = PageRequest.of(page, size);
        UserProject up = UserProject.builder()
                .userId(user.getId())
                .project(project)
                .role(ProjectRole.DEVELOPER)
                .assignedById(getCurrentUserId())
                .build();

        //Page<Commit> commitPage = commitRepository.findByBranchIdOrderByCommittedAtDesc(branchId, pageable);
        userProjectRepository.save(up);
    }

        Page<Commit> commitPage = commitRepository.findByProjectIdOrderByCommittedAtDesc(projectId, pageable);
    // =========================
    // DELETE PROJECT
    // =========================
    @Override
    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

        log.info("Nombre de commits trouvés pour branchId {} : {}", branchId, commitPage.getTotalElements());
    // =========================
    // GHOST USER
    // =========================
    @Override
    @Transactional
    public void assignToGhostUser(Long deletedUserId) {

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
        userProjectRepository.deleteByUserId(deletedUserId);

        List<Project> projects = projectRepository.findByManagedById(deletedUserId);

        for (Project p : projects) {
            p.setManagedById(0L);
        }

        projectRepository.saveAll(projects);
    }
    // --- Méthodes privées d'aide ---

    // =========================
    // ACCESS
    // =========================
    private Set<Project> fetchAccessibleProjects() {
        Long currentUserId = getCurrentUserId();

        Long userId = getCurrentUserId();
        Long companyId = getCurrentCompanyId();
        boolean isSuperAdmin = isCurrentUserSuperAdmin();
        boolean canViewAllInMyCompany = isCurrentUserWorkspaceOwner() || isCurrentUserCompanyAdmin();

        Set<Project> accessibleProjects = new HashSet<>();

        Set<Project> projects = new HashSet<>();
        if (isSuperAdmin) {
            accessibleProjects.addAll(projectRepository.findAll());
            return accessibleProjects;
        }

        projects.addAll(projectRepository.findAssignedProjects(userId));
        accessibleProjects.addAll(projectRepository.findAssignedProjects(currentUserId));

        if (companyId != null) {
            projects.addAll(projectRepository.findByCompanyId(companyId));
        if (canViewAllInMyCompany && companyId != null) {
            accessibleProjects.addAll(projectRepository.findByCompanyId(companyId));
        }

        return projects;
        return accessibleProjects;
    }

    // =========================
    // MAPPING
    // =========================
    private ProjectDTO mapToProjectDto(Project project, UserSummaryDTO manager) {
    private ProjectDTO mapToProjectDto(Project project) {
        return mapToProjectDtoWithManagerData(project, null);
    }

    private ProjectDTO mapToProjectDtoWithManagerData(Project project, UserSummaryDTO managerData) {
        ProjectDTO.ManagerDTO managerDTO = null;

        if (manager != null) {
        if (managerData != null) {
            managerDTO = ProjectDTO.ManagerDTO.builder()
                    .id(manager.getId())
                    .firstName(manager.getFirstName())
                    .lastName(manager.getLastName())
                    .email(manager.getEmail())
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
                .platform(project.getPlatform().name())
                .visibility(project.getVisibility().name())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .manager(managerDTO)
                .build();
    }

    // =========================
    // AUTH SERVICE
    // =========================
    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackManagers")
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

        List<UserSummaryDTO> users =
                authServiceClient.getUsersSummaries("Bearer " + getRawJwt(), ids);

        return users.stream()
                .collect(Collectors.toMap(UserSummaryDTO::getId, u -> u));
    }

    // 👇 LA MÉTHODE DE SECOURS (Fallback) 👇
    // La signature doit être EXACTEMENT la même que getManagersData, plus un paramètre Throwable
    public Map<Long, UserSummaryDTO> fallbackGetManagersData(List<Long> ids, Throwable t) {
        log.warn("⚠️ Disjoncteur ouvert (ou erreur Feign) : Impossible de joindre gitdock-auth. Raison : {}", t.getMessage());
    public Map<Long, UserSummaryDTO> fallbackManagers(List<Long> ids, Throwable t) {

        Map<Long, UserSummaryDTO> fallbackMap = new HashMap<>();
        Map<Long, UserSummaryDTO> map = new HashMap<>();

        // On crée un faux utilisateur générique pour que le Frontend affiche "Service Indisponible"
        // au lieu de juste "Inconnu" ou de planter.
        for (Long id : ids) {
            fallbackMap.put(id, UserSummaryDTO.builder()
            map.put(id, UserSummaryDTO.builder()
                    .id(id)
                    .firstName("Service")
                    .lastName("Down")
                    .email("offline@gitdock.com")
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
        return map;
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

    // =========================
    // JWT
    // =========================
    private String getRawJwt() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;
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
    @Override
    public List<CollaboratorGroupedDTO> getCollaboratorsGrouped() {

        Set<Project> projects = fetchAccessibleProjects();
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

        List<Long> userIds = projects.stream()
                .flatMap(p -> p.getUserProjects().stream())
    public List<CollaboratorGroupedDTO.CollaboratorDTO> getProjectCollaborators(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Récupère tous les IDs des utilisateurs de ce projet
        List<Long> userIds = project.getUserProjects().stream()
                .map(UserProject::getUserId)
                .distinct()
                .toList();

        Map<Long, UserSummaryDTO> users = getManagersData(userIds);

        List<CollaboratorGroupedDTO> result = new ArrayList<>();

        for (Project project : projects) {
                .collect(Collectors.toList());

        // Va chercher leurs infos (Nom, Prénom, Email) dans gitdock-auth via Feign
        Map<Long, UserSummaryDTO> usersMap = getManagersData(userIds);
            List<CollaboratorGroupedDTO.CollaboratorDTO> collaborators =
                    project.getUserProjects().stream()
                            .map(up -> {

                                UserSummaryDTO user = users.get(up.getUserId());

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
                                return CollaboratorGroupedDTO.CollaboratorDTO.builder()
                                        .id(up.getUserId())
                                        .firstName(user != null ? user.getFirstName() : "Unknown")
                                        .lastName(user != null ? user.getLastName() : "")
                                        .email(user != null ? user.getEmail() : "")
                                        .role(up.getRole().name())
                                        .status(user != null && user.isEnabled() ? "active" : "disabled")
                                        .build();
                            })
                            .toList();

            result.add(CollaboratorGroupedDTO.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .collaborators(collaborators)
                    .build());
        if (!canDelete) {
            throw new RuntimeException("Accès refusé : Vous n'avez pas le droit de supprimer ce projet.");
        }

        return result;
        // Hibernate s'occupera de supprimer les entités liées (Commits, Branches,
        // UserProject)
        // si le "cascade = CascadeType.ALL" est bien configuré sur l'entité Project.
        projectRepository.delete(project);
        log.info("Projet supprimé avec succès : {}", project.getName());
    }

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
