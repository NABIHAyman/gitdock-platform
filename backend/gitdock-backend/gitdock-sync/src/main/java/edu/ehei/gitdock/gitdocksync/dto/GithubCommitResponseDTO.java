package edu.ehei.gitdock.gitdocksync.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class GithubCommitResponseDTO {
    private String sha;
    private CommitDetail commit;

    @JsonProperty("html_url")
    private String htmlUrl;

    private Stats stats;

    @Data
    public static class CommitDetail {
        private Author author;
        private String message;
    }

    @Data
    public static class Author {
        private String name;
        private String email;

        private OffsetDateTime date;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        private Integer additions;
        private Integer deletions;
        private Integer total;
    }

    private String diff;
}
