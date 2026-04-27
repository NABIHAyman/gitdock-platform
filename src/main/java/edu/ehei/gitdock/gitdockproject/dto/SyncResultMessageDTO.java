package edu.ehei.gitdock.gitdockproject.dto; // ⚠️ À adapter
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncResultMessageDTO {
    private Long projectId;
    private List<GithubBranchResponseDTO> branches;
    private List<GithubCommitResponseDTO> commits;
    private boolean success;
    private String errorMessage;
    private long durationSeconds;
    private String repoUrl;
}