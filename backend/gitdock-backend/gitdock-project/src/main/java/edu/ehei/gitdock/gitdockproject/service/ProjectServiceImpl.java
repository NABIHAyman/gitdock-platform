package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.*;
import edu.ehei.gitdock.gitdockproject.enums.*;
import edu.ehei.gitdock.gitdockproject.model.*;
import edu.ehei.gitdock.gitdockproject.repository.*;
import edu.ehei.gitdock.gitdockproject.security.JwtService;
import edu.ehei.gitdock.gitdockproject.service.interfaces.IProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final AuthServiceClient authServiceClient;
    private final HttpServletRequest request;
    private final JwtService jwtService;
    private final RabbitTemplate rabbitTemplate;

    // =========================
    // CREATE PROJECT
    // =========================
    @Override
    @Transactional
    public ProjectDTO createProject(CreateProjectRequestDTO dto) {

        Long userId = getCurrentUserId();
        Long companyId = getCurrentCompanyId();

        if (companyId == null) {
            throw new RuntimeException("Company manquante dans le token");
        }

        Project project = Project.builder()
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

        project = projectRepository.save(project);

        UserProject up = UserProject.builder()
                .userId(userId)
                .project(project)
                .role(ProjectRole.MANAGER)
                .assignedAt(LocalDateTime.now())
                .assignedById(userId)
                .build();

        userProjectRepository.save(up);

        return mapToProjectDto(project, null);
    }

    // =========================
    // GET ALL PROJECTS
    // =========================
    @Override
    public List<ProjectDTO> getAllProjects() {

        Set<Project> projects = fetchAccessibleProjects();

        List<Long> managerIds = projects.stream()
                .map(Project::getManagedById)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, UserSummaryDTO> managers = getManagersData(managerIds);

        return projects.stream()
                .map(p -> mapToProjectDto(p, managers.get(p.getManagedById())))
                .sorted(Comparator.comparing(ProjectDTO::getCreatedAt).reversed())
                .toList();
    }

    // =========================
    // REMOVE COLLABORATOR (FIX IMPORTANT)
    // =========================
    @Override
    @Transactional
    public void removeCollaborator(Long projectId, Long collaboratorId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        Long currentUserId = getCurrentUserId();

        boolean isManager = project.getManagedById() != null
                && project.getManagedById().equals(currentUserId);

        if (!isManager) {
            throw new RuntimeException("Accès refusé");
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
    }

    // =========================
    // BRANCHES
    // =========================
    @Override
    public List<BranchDTO> getProjectBranches(Long projectId) {

        return branchRepository.findByProjectId(projectId)
                .stream()
                .map(b -> BranchDTO.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .lastUpdated(b.getUpdatedAt())
                        .build())
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

        UserSummaryDTO user = authServiceClient.getUserByEmail(
                "Bearer " + getRawJwt(),
                dto.getEmail()
        );

        UserProject up = UserProject.builder()
                .userId(user.getId())
                .project(project)
                .role(ProjectRole.DEVELOPER)
                .assignedById(getCurrentUserId())
                .build();

        userProjectRepository.save(up);
    }

    // =========================
    // DELETE PROJECT
    // =========================
    @Override
    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    // =========================
    // GHOST USER
    // =========================
    @Override
    @Transactional
    public void assignToGhostUser(Long deletedUserId) {

        userProjectRepository.deleteByUserId(deletedUserId);

        List<Project> projects = projectRepository.findByManagedById(deletedUserId);

        for (Project p : projects) {
            p.setManagedById(0L);
        }

        projectRepository.saveAll(projects);
    }

    // =========================
    // ACCESS
    // =========================
    private Set<Project> fetchAccessibleProjects() {

        Long userId = getCurrentUserId();
        Long companyId = getCurrentCompanyId();

        Set<Project> projects = new HashSet<>();

        projects.addAll(projectRepository.findAssignedProjects(userId));

        if (companyId != null) {
            projects.addAll(projectRepository.findByCompanyId(companyId));
        }

        return projects;
    }

    // =========================
    // MAPPING
    // =========================
    private ProjectDTO mapToProjectDto(Project project, UserSummaryDTO manager) {

        ProjectDTO.ManagerDTO managerDTO = null;

        if (manager != null) {
            managerDTO = ProjectDTO.ManagerDTO.builder()
                    .id(manager.getId())
                    .firstName(manager.getFirstName())
                    .lastName(manager.getLastName())
                    .email(manager.getEmail())
                    .build();
        }

        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .url(project.getUrl())
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
    public Map<Long, UserSummaryDTO> getManagersData(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }

        List<UserSummaryDTO> users =
                authServiceClient.getUsersSummaries("Bearer " + getRawJwt(), ids);

        return users.stream()
                .collect(Collectors.toMap(UserSummaryDTO::getId, u -> u));
    }

    public Map<Long, UserSummaryDTO> fallbackManagers(List<Long> ids, Throwable t) {

        Map<Long, UserSummaryDTO> map = new HashMap<>();

        for (Long id : ids) {
            map.put(id, UserSummaryDTO.builder()
                    .id(id)
                    .firstName("Service")
                    .lastName("Down")
                    .email("offline@gitdock.com")
                    .isEnabled(false)
                    .build());
        }

        return map;
    }

    // =========================
    // JWT
    // =========================
    private String getRawJwt() {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;
    }

    private Long getCurrentUserId() {
        String jwt = getRawJwt();
        return jwt != null ? jwtService.extractUserId(jwt) : null;
    }

    private Long getCurrentCompanyId() {
        String jwt = getRawJwt();
        return jwt != null ? jwtService.extractCompanyId(jwt) : null;
    }
    @Override
    public List<CollaboratorGroupedDTO> getCollaboratorsGrouped() {

        Set<Project> projects = fetchAccessibleProjects();

        List<Long> userIds = projects.stream()
                .flatMap(p -> p.getUserProjects().stream())
                .map(UserProject::getUserId)
                .distinct()
                .toList();

        Map<Long, UserSummaryDTO> users = getManagersData(userIds);

        List<CollaboratorGroupedDTO> result = new ArrayList<>();

        for (Project project : projects) {

            List<CollaboratorGroupedDTO.CollaboratorDTO> collaborators =
                    project.getUserProjects().stream()
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

            result.add(CollaboratorGroupedDTO.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .collaborators(collaborators)
                    .build());
        }

        return result;
    }
}