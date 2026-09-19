package edu.ehei.gitdock.gitdockproject.dto;

import lombok.Builder;
import lombok.Data;

// CollaboratorAddedEventDTO.java
@Data
@Builder
public class CollaboratorAddedEventDTO {
    private Long userId;
    private Long projectId;
    private String role;
}