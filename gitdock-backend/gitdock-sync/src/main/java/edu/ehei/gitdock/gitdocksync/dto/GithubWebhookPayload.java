package edu.ehei.gitdock.gitdocksync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Très important pour ignorer les 90% du JSON de GitHub qu'on n'utilise pas
public class GithubWebhookPayload {

    private String ref; // Ex: "refs/heads/main"
    private Repository repository;
    private List<Commit> commits;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("html_url")
        private String htmlUrl; // L'URL du projet pour faire la liaison

        @JsonProperty("full_name")
        private String fullName; // "owner/repo"
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String id; // Le SHA
        private String message;
        private String timestamp;
        private Author author;
        private List<String> added;
        private List<String> removed;
        private List<String> modified;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
    }
}