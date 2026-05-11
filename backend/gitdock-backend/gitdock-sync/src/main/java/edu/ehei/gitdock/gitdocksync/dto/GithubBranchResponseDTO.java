package edu.ehei.gitdock.gitdocksync.dto;
import lombok.Data;

@Data
public class GithubBranchResponseDTO {
    private String name;
    private CommitInfo commit;

    @Data
    public static class CommitInfo {
        private String sha;
        private String url;
    }
}
