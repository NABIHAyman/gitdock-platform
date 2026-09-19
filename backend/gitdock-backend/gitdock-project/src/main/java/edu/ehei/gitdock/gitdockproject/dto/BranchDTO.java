package edu.ehei.gitdock.gitdockproject.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class BranchDTO implements Serializable {
    private Long id;
    private String name;
    private LocalDateTime lastUpdated;
    // On pourra ajouter le dernier commit ici plus tard
}
