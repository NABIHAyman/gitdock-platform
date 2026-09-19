package edu.ehei.gitdock.gitdockproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String repoUrl;
    private String status; // On renvoie le statut sous forme de texte (PENDING, ACTIVE...)
    private Long managerId;
}