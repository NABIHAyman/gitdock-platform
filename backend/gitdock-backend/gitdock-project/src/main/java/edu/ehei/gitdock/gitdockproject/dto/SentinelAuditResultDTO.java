package edu.ehei.gitdock.gitdockproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class SentinelAuditResultDTO {

    private String type;

    @JsonProperty("commit_hash")
    private String commitHash;

    @JsonProperty("project_id")
    private String projectId;

    private String author;

    @JsonProperty("is_clean")
    private boolean isClean;

    private String summary;

    private List<VulnerabilityDTO> vulnerabilities;

    @Data
    @NoArgsConstructor
    public static class VulnerabilityDTO {
        private String severity;
        private String type;
        @JsonProperty("line_snippet")
        private String lineSnippet;
        private String recommendation;
    }
}