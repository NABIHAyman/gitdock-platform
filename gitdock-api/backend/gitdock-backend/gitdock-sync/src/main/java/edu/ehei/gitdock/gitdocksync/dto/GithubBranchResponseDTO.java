package edu.ehei.gitdock.gitdocksync.dto;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class GithubBranchResponseDTO {
    private String name;
    private CommitInfo commit;
    private OffsetDateTime date;

    @Data
    public static class CommitInfo {
        private String sha;
        private String url;
    }
}
