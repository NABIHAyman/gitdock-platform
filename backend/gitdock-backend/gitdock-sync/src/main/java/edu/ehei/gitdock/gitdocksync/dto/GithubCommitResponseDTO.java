package edu.ehei.gitdock.gitdocksync.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

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

    private List<FileDetail> files;

    public List<FileDetail> getFiles() { return files; }
    public void setFiles(List<FileDetail> files) { this.files = files; }

    public static class FileDetail {
        private String filename;
        private String patch; // C'est ici que GitHub cache le diff !

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getPatch() { return patch; }
        public void setPatch(String patch) { this.patch = patch; }
    }
}
