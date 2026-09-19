package edu.ehei.gitdock.gitdockproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SagaProjectCreationRequestDTO {
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String repoUrl;

    @NotEmpty
    private List<Long> teamIds; // Les fameux IDs [2, 5, 8] renvoyés par GitDock-YAM
}
