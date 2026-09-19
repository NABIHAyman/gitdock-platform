package edu.ehei.gitdock.gitdocksync.dto;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommitEnrichmentResultDTO {
    private Long projectId;
    private String commitSha;
    private Integer additions;
    private Integer deletions;
    private Long authorUserId;
}