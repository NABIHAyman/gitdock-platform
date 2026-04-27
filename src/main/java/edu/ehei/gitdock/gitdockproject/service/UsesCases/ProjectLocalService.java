package edu.ehei.gitdock.gitdockproject.service.UsesCases;

import edu.ehei.gitdock.gitdockproject.enums.ProjectRole;
import edu.ehei.gitdock.gitdockproject.enums.ProjectStatus;
import edu.ehei.gitdock.gitdockproject.dto.SagaProjectCreationRequestDTO;
import edu.ehei.gitdock.gitdockproject.exception.ResourceNotFoundException;
import edu.ehei.gitdock.gitdockproject.model.Project;
import edu.ehei.gitdock.gitdockproject.model.UserProject;
import edu.ehei.gitdock.gitdockproject.repository.ProjectRepository;
import edu.ehei.gitdock.gitdockproject.repository.UserProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import edu.ehei.gitdock.gitdockproject.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectLocalService {

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    @Transactional // Transaction ACID locale (Commit immédiat à la fin de la méthode)
    public Project createProjectWithStatus(SagaProjectCreationRequestDTO request, Long managerId, Long companyId, ProjectStatus status) {
        Project project = new Project();
        project.setName(request.getName());
        project.setUrl(request.getRepoUrl());
        project.setManagedById(managerId);
        project.setStatus(status);


        project.setCompanyId(companyId);

        project.setPlatform(edu.ehei.gitdock.gitdockproject.enums.ProjectPlatform.GITHUB);
        project.setVisibility(edu.ehei.gitdock.gitdockproject.enums.ProjectVisibility.PRIVATE);

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProjectStatus(Long projectId, ProjectStatus newStatus) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable"));
        project.setStatus(newStatus);
        return projectRepository.save(project);
    }

    @Transactional
    public void hardDeleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }

    @Transactional
    public void assignTeamToProject(Long projectId, List<Long> teamIds, Long managerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable"));

        // On assigne chaque membre recommandé par l'IA avec le rôle DEVELOPER par défaut
        for (Long userId : teamIds) {
            UserProject userProject = UserProject.builder()
                    .userId(userId)
                    .project(project)
                    .role(ProjectRole.DEVELOPER)
                    .assignedById(managerId)
                    .assignedAt(LocalDateTime.now())
                    .build();

            userProjectRepository.save(userProject);
        }
    }
}
