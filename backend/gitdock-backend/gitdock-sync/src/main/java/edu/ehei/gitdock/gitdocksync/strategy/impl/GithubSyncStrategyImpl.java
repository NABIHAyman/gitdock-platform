package edu.ehei.gitdock.gitdocksync.strategy.impl;

import edu.ehei.gitdock.gitdocksync.client.AuthServiceClient;
import edu.ehei.gitdock.gitdocksync.dto.*;
import edu.ehei.gitdock.gitdocksync.strategy.IGitPlatformSyncStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubSyncStrategyImpl implements IGitPlatformSyncStrategy {

    private final AuthServiceClient authServiceClient;
    private final RestClient restClient = RestClient.create();

    // L'API par défaut (tu peux aussi la mettre dans application.yml avec @Value)
    private final String GITHUB_API_URL = "https://api.github.com";

    @Override
    public boolean supports(String platform) {
        return "GITHUB".equalsIgnoreCase(platform);
    }

    @Override
    public SyncResultDTO fetchProjectData(String repoUrl, Long userId) throws Exception {
        log.info("🔄 [GITHUB] Début de l'aspiration pour l'URL : {} (User: {})", repoUrl, userId);
        long startTime = System.currentTimeMillis();

        try {
            // 1. Récupérer le token GitHub
            String token = null;
            try {
                token = authServiceClient.getGithubToken(userId);
            } catch (Exception e) {
                log.warn("⚠️ Aucun token GitHub trouvé pour l'utilisateur {}. Tentative d'accès public.", userId);
            }
            final String finalToken = token;

            // 2. Extraire owner/repo
            String[] repoInfo = extractRepoInfo(repoUrl);
            String owner = repoInfo[0];
            String repo = repoInfo[1];

            // 3. Aspirer les branches
            String branchesUrl = String.format("%s/repos/%s/%s/branches?per_page=100", GITHUB_API_URL, owner, repo);
            GithubBranchResponseDTO[] branches = restClient.get()
                    .uri(branchesUrl)
                    .headers(h -> {
                        if (finalToken != null) h.setBearerAuth(finalToken);
                        h.set("Accept", "application/vnd.github+json");
                    })
                    .retrieve()
                    .body(GithubBranchResponseDTO[].class);

            // 4. Aspirer la liste des commits (On limite à 30 pour éviter de surcharger l'API GitHub d'un coup)
            String commitsUrl = String.format("%s/repos/%s/%s/commits?per_page=30", GITHUB_API_URL, owner, repo);
            GithubCommitResponseDTO[] basicCommits = restClient.get()
                    .uri(commitsUrl)
                    .headers(h -> {
                        if (finalToken != null) h.setBearerAuth(finalToken);
                        h.set("Accept", "application/vnd.github+json");
                    })
                    .retrieve()
                    .body(GithubCommitResponseDTO[].class);

            // 5. 🚀 LE DEEP-FETCH : On va chercher les Stats pour chaque commit
            List<GithubCommitResponseDTO> enrichedCommits = new java.util.ArrayList<>();
            if (basicCommits != null) {
                log.info("🔍 Récupération des statistiques (additions/deletions) pour {} commits...", basicCommits.length);
                for (GithubCommitResponseDTO basicCommit : basicCommits) {
                    try {
                        String detailUrl = String.format("%s/repos/%s/%s/commits/%s", GITHUB_API_URL, owner, repo, basicCommit.getSha());
                        GithubCommitResponseDTO detailedCommit = restClient.get()
                                .uri(detailUrl)
                                .headers(h -> {
                                    if (finalToken != null) h.setBearerAuth(finalToken);
                                    h.set("Accept", "application/vnd.github+json");
                                })
                                .retrieve()
                                .body(GithubCommitResponseDTO.class);

                        // On ajoute le commit enrichi avec ses stats !
                        enrichedCommits.add(detailedCommit != null ? detailedCommit : basicCommit);
                    } catch (Exception e) {
                        log.warn("⚠️ Impossible de récupérer les stats pour le commit {} : {}", basicCommit.getSha(), e.getMessage());
                        enrichedCommits.add(basicCommit); // On le garde même sans stats
                    }
                }
            }

            // Simulation d'une synchro longue (pour test)
            // Thread.sleep(11000);

            long durationSeconds = (System.currentTimeMillis() - startTime) / 1000;
            log.info("✅ Aspiration terminée en {}s", durationSeconds);

            // Construire le résultat avec les métadonnées
            return SyncResultDTO.builder()
                    .branches(branches != null ? Arrays.asList(branches) : List.of())
                    .commits(enrichedCommits)
                    .platform("GITHUB")
                    .durationSeconds(durationSeconds)
                    .success(true)
                    .build();

        } catch (Exception e) {
            long durationSeconds = (System.currentTimeMillis() - startTime) / 1000;
            log.error("❌ Erreur lors de l'aspiration GitHub: {}", e.getMessage(), e);

            return SyncResultDTO.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .platform("GITHUB")
                    .durationSeconds(durationSeconds)
                    .build();
        }
    }

    @Override
    public String[] extractRepoInfo(String repoUrl) throws IllegalArgumentException {
        try {
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
        } catch (Exception e) {
            throw new IllegalArgumentException("URL GitHub invalide: " + repoUrl, e);
        }
    }
}