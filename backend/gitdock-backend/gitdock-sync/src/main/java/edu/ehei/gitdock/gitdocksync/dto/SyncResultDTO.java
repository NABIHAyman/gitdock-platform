package edu.ehei.gitdock.gitdocksync.dto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SyncResultDTO {
    private List<GithubBranchResponseDTO> branches;
    private List<GithubCommitResponseDTO> commits;
    private String platform;
    private long durationSeconds;
    private boolean success;
    private String errorMessage;
}