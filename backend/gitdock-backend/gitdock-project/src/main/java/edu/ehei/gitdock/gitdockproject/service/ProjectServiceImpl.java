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
    private final AuthServiceClient authServiceClient;
    private final HttpServletRequest request;
    private final JwtService jwtService;

    @Override
    @Transactional
    public ProjectDTO createProject(CreateProjectRequestDTO requestDto) {
        Long currentUserId = getCurrentUserId();
        Long companyId = getCurrentCompanyId();

        if (companyId == null) {
            throw new RuntimeException("Impossible de créer un projet : L'utilisateur n'est rattaché à aucune entreprise.");
        }

        Project project = Project.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .url(requestDto.getUrl())
                .platform(ProjectPlatform.valueOf(requestDto.getPlatform().toUpperCase()))
                .visibility(ProjectVisibility.valueOf(requestDto.getVisibility().toUpperCase()))
                .companyId(companyId)
                .managedById(currentUserId)
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

        SyncRequestMessageDTO message = SyncRequestMessageDTO.builder()
                .projectId(savedProject.getId())
                .repoUrl(savedProject.getUrl())
                .userId(currentUserId)
                .platform(savedProject.getPlatform().name())
                .projectName(savedProject.getName())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_REQUEST, message);

        return mapToProjectDto(savedProject);
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        // fetchAccessibleProjects retourne déjà les bons projets selon le rôle
        // (super admin → tous, sinon → projets où l'user est assigné via UserProject)
        Set<Project> accessibleProjects = fetchAccessibleProjects();

        List<Long> managerIds = accessibleProjects.stream()
                .map(Project::getManagedById)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, UserSummaryDTO> managerMap = getManagersData(managerIds);

        return accessibleProjects.stream()
                .map(p -> mapToProjectDtoWithManagerData(p, managerMap.get(p.getManagedById())))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CollaboratorGroupedDTO> getCollaboratorsGrouped() {
        Set<Project> visibleProjects = fetchAccessibleProjects();
        List<Long> userIdsToFetch = visibleProjects.stream()
                .flatMap(p -> p.getUserProjects().stream())
                .map(UserProject::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, UserSummaryDTO> usersMap = getManagersData(userIdsToFetch);

        return visibleProjects.stream()
                .map(p -> CollaboratorGroupedDTO.builder()
                        .projectId(p.getId())
                        .projectName(p.getName())
                        .collaborators(p.getUserProjects().stream()
                                .map(up -> mapToCollaboratorDto(up, usersMap.get(up.getUserId())))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CollaboratorGroupedDTO.CollaboratorDTO> getProjectCollaborators(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        verifyProjectAccess(project);

        List<Long> userIds = project.getUserProjects().stream()
                .map(UserProject::getUserId)
                .collect(Collectors.toList());

        Map<Long, UserSummaryDTO> usersMap = getManagersData(userIds);

        return project.getUserProjects().stream()
                .map(up -> mapToCollaboratorDto(up, usersMap.get(up.getUserId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addCollaborator(Long projectId, AddCollaboratorDTO request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        UserSummaryDTO user;
        try {
            user = authServiceClient.getUserByEmail(request.getEmail());
        } catch (Exception e) {
            user = inviteNewUser(request, project);
        }

        UserProject userProject = UserProject.builder()
                .userId(user.getId())
                .project(project)
                .role(ProjectRole.valueOf(request.getRole().toUpperCase()))
                .assignedById(getCurrentUserId())
                .build();

        userProjectRepository.save(userProject);
    }

    @Override
    @Transactional
    public void removeCollaborator(Long projectId, Long collaboratorId) {
        userProjectRepository.removeUserFromProject(projectId, collaboratorId);
    }

    @Override
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // ✅ Vérification : l'user doit être assigné à ce projet
        verifyProjectAccess(project);

        UserSummaryDTO managerInfo = project.getManagedById() != null ?
                getManagersData(List.of(project.getManagedById())).get(project.getManagedById()) : null;

        return mapToProjectDtoWithManagerData(project, managerInfo);
    }

    @Override
    public List<BranchDTO> getProjectBranches(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));
        verifyProjectAccess(project);
        return branchRepository.findByProjectId(projectId).stream()
                .map(b -> BranchDTO.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .lastUpdated(b.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

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

    // --- LOGIQUE RESILIENCE4J ---

    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackGetManagersData")
    public Map<Long, UserSummaryDTO> getManagersData(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashMap<>();
        return authServiceClient.getUsersSummaries(ids).stream()
                .collect(Collectors.toMap(UserSummaryDTO::getId, u -> u));
    }

    public Map<Long, UserSummaryDTO> fallbackGetManagersData(List<Long> ids, Throwable t) {
        log.warn("Fallback authService: {}", t.getMessage());
        return ids.stream().collect(Collectors.toMap(id -> id, id -> UserSummaryDTO.builder()
                .id(id).firstName("Indisponible").lastName("").email("").isEnabled(false).build()));
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void assignToGhostUser(Long deletedUserId) {
        List<Project> projects = projectRepository.findByManagedById(deletedUserId);
        projects.forEach(p -> p.setManagedById(0L));
        projectRepository.saveAll(projects);
    }

    // --- HELPERS ---

    private CollaboratorGroupedDTO.CollaboratorDTO mapToCollaboratorDto(UserProject up, UserSummaryDTO userInfo) {
        return CollaboratorGroupedDTO.CollaboratorDTO.builder()
                .id(up.getUserId())
                .firstName(userInfo != null ? userInfo.getFirstName() : "Inconnu")
                .lastName(userInfo != null ? userInfo.getLastName() : "Inconnu")
                .role(up.getRole().name())
                .status((userInfo != null && userInfo.isEnabled()) ? "active" : "pending")
                .build();
    }

    private UserSummaryDTO inviteNewUser(AddCollaboratorDTO request, Project project) {
        return authServiceClient.inviteUser(new InviteCollaboratorRequestDTO(
                request.getEmail(), request.getFirstName(), request.getLastName(),
                project.getName(), project.getCompanyId()));
    }

    /**
     * Retourne les projets accessibles selon le rôle de l'utilisateur courant :
     * - Super admin → tous les projets
     * - Sinon → uniquement les projets où l'user est dans UserProject (manager OU collaborateur)
     */
    private Set<Project> fetchAccessibleProjects() {
        if (isCurrentUserSuperAdmin()) {
            return new HashSet<>(projectRepository.findAll());
        }
        // findAssignedProjects retourne les projets où getCurrentUserId() est dans user_project
        // donc ça couvre aussi bien le manager que le dev/tester/consultant
        return new HashSet<>(projectRepository.findAssignedProjects(getCurrentUserId()));
    }

    /**
     * Vérifie que l'utilisateur courant a accès à ce projet :
     * - Super admin → toujours OK
     * - Sinon → doit être dans la table user_project pour ce projet
     */
    private void verifyProjectAccess(Project project) {
        if (isCurrentUserSuperAdmin()) return;

        Long currentUserId = getCurrentUserId();

        // ✅ Vérifie si l'user est assigné à ce projet (manager OU collaborateur)
        boolean isAssigned = project.getUserProjects().stream()
                .anyMatch(up -> up.getUserId().equals(currentUserId));

        if (!isAssigned) {
            throw new RuntimeException("Accès interdit à ce projet");
        }
    }

    private ProjectDTO mapToProjectDto(Project project) {
        return mapToProjectDtoWithManagerData(project, null);
    }

    private ProjectDTO mapToProjectDtoWithManagerData(Project project, UserSummaryDTO managerData) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .url(project.getUrl())
                .platform(project.getPlatform().name().toLowerCase())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private String getRawJwt() {
        String auth = request.getHeader("Authorization");
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
    }

    private Long getCurrentUserId() { return jwtService.extractUserId(getRawJwt()); }
    private Long getCurrentCompanyId() { return jwtService.extractCompanyId(getRawJwt()); }
    private boolean isCurrentUserSuperAdmin() {
        String jwt = getRawJwt();
        return jwt != null && jwtService.extractRoles(jwt).contains("ROLE_SUPER_ADMIN");
    }
}