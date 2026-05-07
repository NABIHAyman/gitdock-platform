package edu.ehei.gitdock.gitdocksync.strategy.impl;

import edu.ehei.gitdock.gitdocksync.client.AuthServiceClient;
import edu.ehei.gitdock.gitdocksync.dto.*;
import edu.ehei.gitdock.gitdocksync.messaging.KafkaProducerService;
import edu.ehei.gitdock.gitdocksync.strategy.IGitPlatformSyncStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class GithubSyncStrategyImpl implements IGitPlatformSyncStrategy {

    private final AuthServiceClient authServiceClient;
    private final KafkaProducerService kafkaProducerService;
    private final RestClient restClient;

    // Injection via application.yml pour faciliter les tests et la conf
    @Value("${github.api.url:https://api.github.com}")
    private String githubApiUrl;

    // Injection du Builder pour profiter de la configuration Spring (Timeouts, Interceptors)
    public GithubSyncStrategyImpl(AuthServiceClient authServiceClient,
                                  KafkaProducerService kafkaProducerService,
                                  RestClient.Builder restClientBuilder) {
        this.authServiceClient = authServiceClient;
        this.kafkaProducerService = kafkaProducerService;
        this.restClient = restClientBuilder.build(); // Le baseUrl sera appliqué dans les requêtes
    }

    @Override
    public boolean supports(String platform) {
        return "GITHUB".equalsIgnoreCase(platform);
    }

    @Override
    public SyncResultDTO fetchProjectData(String repoUrl, Long userId) {
        log.info("🔄 [GITHUB] Début de l'aspiration pour l'URL : {} (User: {})", repoUrl, userId);
        long startTime = System.currentTimeMillis();

        try {
            // 1. Gestion propre du token avec Optional
            String token = null;
            try {
                token = authServiceClient.getGithubToken(userId);
            } catch (Exception e) {
                log.warn("⚠️ Aucun token GitHub trouvé pour l'utilisateur {}. Mode public activé.", userId);
            }
            final String finalToken = token;

            String[] repoInfo = extractRepoInfo(repoUrl);
            String owner = repoInfo[0];
            String repo = repoInfo[1];

            // 2. Utilisation des URI Templates de Spring (Sécurisé contre les injections/espaces)
            GithubBranchResponseDTO[] branches = restClient.get()
                    .uri(githubApiUrl + "/repos/{owner}/{repo}/branches?per_page=100", owner, repo)
                    .headers(h -> {
                        if (finalToken != null) h.setBearerAuth(finalToken);
                        h.set("Accept", "application/vnd.github+json");
                    })
                    .retrieve()
                    .body(GithubBranchResponseDTO[].class);

            GithubCommitResponseDTO[] basicCommits = restClient.get()
                    .uri(githubApiUrl + "/repos/{owner}/{repo}/commits?per_page=30", owner, repo)
                    .headers(h -> {
                        if (finalToken != null) h.setBearerAuth(finalToken);
                        h.set("Accept", "application/vnd.github+json");
                    })
                    .retrieve()
                    .body(GithubCommitResponseDTO[].class);

            List<GithubCommitResponseDTO> commitsToEnrich = basicCommits != null ?
                    Arrays.stream(basicCommits).limit(15).toList() : List.of();

            List<GithubCommitResponseDTO> enrichedCommits = new ArrayList<>();

            // 3. Boucle de Deep-Fetch (Attention : Reste un goulot d'étranglement potentiel)
            for (GithubCommitResponseDTO basicCommit : commitsToEnrich) {
                String commitSha = basicCommit.getSha();

                try {
                    // Fetch Stats
                    GithubCommitResponseDTO detailedCommit = restClient.get()
                            .uri(githubApiUrl + "/repos/{owner}/{repo}/commits/{sha}", owner, repo, commitSha)
                            .headers(h -> {
                                if (finalToken != null) h.setBearerAuth(finalToken);
                                h.set("Accept", "application/vnd.github+json");
                            })
                            .retrieve()
                            .body(GithubCommitResponseDTO.class);

                    // Fetch Diff
                    String diffContent = restClient.get()
                            .uri(githubApiUrl + "/repos/{owner}/{repo}/commits/{sha}", owner, repo, commitSha)
                            .headers(h -> {
                                if (finalToken != null) h.setBearerAuth(finalToken);
                                h.set("Accept", "application/vnd.github.v3.diff");
                            })
                            .retrieve()
                            .body(String.class);

                    GithubCommitResponseDTO finalCommit = detailedCommit != null ? detailedCommit : basicCommit;
                    finalCommit.setDiff(diffContent);
                    enrichedCommits.add(finalCommit);

                    // 4. Envoi Kafka asynchrone (ne bloque pas si Kafka ralentit)
                    if (diffContent != null && !diffContent.isBlank()) {
                        String authorName = Optional.ofNullable(finalCommit.getCommit())
                                .map(GithubCommitResponseDTO.CommitDetail::getAuthor)
                                .map(GithubCommitResponseDTO.Author::getName)
                                .orElse("Unknown"); // Évite les NullPointerExceptions agressifs

                        kafkaProducerService.sendCommitToSentinel(
                                commitSha,
                                owner + "/" + repo,
                                diffContent,
                                authorName
                        );
                    }

                } catch (Exception e) {
                    log.error("⚠️ Échec stats/diff pour le commit {} : {}", commitSha, e.getMessage());
                    enrichedCommits.add(basicCommit); // Fallback propre
                }
            }

            long durationSeconds = (System.currentTimeMillis() - startTime) / 1000;
            log.info("✅ Aspiration terminée en {}s", durationSeconds);

            return SyncResultDTO.builder()
                    .branches(branches != null ? Arrays.asList(branches) : List.of())
                    .commits(enrichedCommits)
                    .platform("GITHUB")
                    .durationSeconds(durationSeconds)
                    .success(true)
                    .build();

        } catch (Exception e) {
            long durationSeconds = (System.currentTimeMillis() - startTime) / 1000;
            log.error("❌ Erreur critique lors de l'aspiration GitHub: {}", e.getMessage(), e);

            return SyncResultDTO.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .platform("GITHUB")
                    .durationSeconds(durationSeconds)
                    .build();
        }
    }

    @Override
    public String[] extractRepoInfo(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new IllegalArgumentException("URL GitHub vide ou nulle.");
        }

        String cleanUrl = repoUrl
                .replace("https://github.com/", "")
                .replace("http://github.com/", "")
                .replace(".git", "")
                .trim();

        String[] parts = cleanUrl.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("URL GitHub invalide. Format attendu: https://github.com/owner/repo");
        }
        return new String[]{parts[0], parts[1]};
    }
}