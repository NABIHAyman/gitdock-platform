package edu.ehei.gitdock.gitdockproject.mapper;

import edu.ehei.gitdock.gitdockproject.dto.ProjectResponseDTO;
import edu.ehei.gitdock.gitdockproject.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
    public static ProjectResponseDTO toResponse(Project project) {
        if (project == null) return null;

        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .repoUrl(project.getUrl()) //
                .status(project.getStatus() != null ? project.getStatus().name() : null)
                .managerId(project.getManagedById())
                .build();
    }
}