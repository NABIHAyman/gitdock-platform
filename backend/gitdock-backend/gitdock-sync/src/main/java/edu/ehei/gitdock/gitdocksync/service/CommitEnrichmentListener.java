package edu.ehei.gitdock.gitdocksync.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.ehei.gitdock.gitdocksync.client.AuthServiceClient;
import edu.ehei.gitdock.gitdocksync.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentRequestDTO;
import edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentResultDTO;
import edu.ehei.gitdock.gitdocksync.dto.GithubCommitResponseDTO;
import edu.ehei.gitdock.gitdocksync.messaging.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class CommitEnrichmentListener {

    private final RabbitTemplate rabbitTemplate;
    private final AuthServiceClient authServiceClient;
    private final KafkaProducerService kafkaProducerService;
    private final RestClient restClient;

    // 👇 INJECTION SÉCURISÉE DU RESTCLIENT
    public CommitEnrichmentListener(RabbitTemplate rabbitTemplate,
                                    AuthServiceClient authServiceClient,
                                    KafkaProducerService kafkaProducerService,
                                    RestClient.Builder restClientBuilder) {
        this.rabbitTemplate = rabbitTemplate;
        this.authServiceClient = authServiceClient;
        this.kafkaProducerService = kafkaProducerService;
        this.restClient = restClientBuilder.build(); // Hérite de la configuration Spring
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ENRICH_REQUEST)
    public void enrichCommit(CommitEnrichmentRequestDTO request) {
        log.info("🔍 Enrichissement demandé pour le commit {}", request.getCommitSha().substring(0, 7));
        try {
            String token = null;
            try {
                token = authServiceClient.getGithubToken(request.getManagerId());
            } catch (Exception e) {
                log.warn("Aucun token trouvé pour managerId {}. Tentative publique.", request.getManagerId());
            }
            final String finalToken = token;

            String cleanUrl = request.getRepoUrl().replace("https://github.com/", "").replace("http://github.com/", "").replace(".git", "");
            String[] parts = cleanUrl.split("/");

            GithubCommitResponseDTO detailedCommit = restClient.get()
                    .uri("https://api.github.com/repos/{owner}/{repo}/commits/{sha}", parts[0], parts[1], request.getCommitSha())
                    .headers(h -> {
                        if (finalToken != null) h.setBearerAuth(finalToken);
                        h.set("Accept", "application/vnd.github+json");
                    })
                    .retrieve().body(GithubCommitResponseDTO.class);

            int additions = detailedCommit != null && detailedCommit.getStats() != null ? detailedCommit.getStats().getAdditions() : 0;
            int deletions = detailedCommit != null && detailedCommit.getStats() != null ? detailedCommit.getStats().getDeletions() : 0;

            StringBuilder diffBuilder = new StringBuilder();
            if (detailedCommit != null && detailedCommit.getFiles() != null) {
                for (var file : detailedCommit.getFiles()) {
                    if (file.getPatch() != null) {
                        diffBuilder.append("--- a/").append(file.getFilename()).append("\n");
                        diffBuilder.append("+++ b/").append(file.getFilename()).append("\n");
                        diffBuilder.append(file.getPatch()).append("\n\n");
                    }
                }
            }
            String diffContent = diffBuilder.toString();
            if (!diffContent.isBlank()) {
                String authorName = detailedCommit.getCommit().getAuthor().getName();
                kafkaProducerService.sendCommitToSentinel(request.getCommitSha(), String.valueOf(request.getProjectId()), diffContent, authorName);
            }

            CommitEnrichmentResultDTO result = new CommitEnrichmentResultDTO(
                    request.getProjectId(), request.getCommitSha(), additions, deletions, request.getAuthorUserId()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_ENRICH_RESULT, result);

        } catch (Exception e) {
            log.error("❌ Échec de l'enrichissement du commit {}", request.getCommitSha(), e);
        }
    }
}