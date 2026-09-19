package edu.ehei.gitdock.gitdockproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitSavedEventDTO {
    private Long commitId;
    private String hash;
    private String message;
    private Long projectId;
    private Long branchId;
    private Long authorUserId; // Très important pour la Gamification (XP)
    private Integer additions;
    private Integer deletions;
}