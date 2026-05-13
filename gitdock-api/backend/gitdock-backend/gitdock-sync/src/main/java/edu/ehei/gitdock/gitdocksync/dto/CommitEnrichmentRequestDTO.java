package edu.ehei.gitdock.gitdocksync.dto;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommitEnrichmentRequestDTO {
    private Long projectId;
    private String commitSha;
    private String repoUrl;
    private Long managerId; // Pour récupérer le Token GitHub
    private Long authorUserId; // Pour la Gamification plus tard
}