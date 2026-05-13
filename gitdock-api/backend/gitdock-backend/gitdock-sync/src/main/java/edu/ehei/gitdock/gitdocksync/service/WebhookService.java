package edu.ehei.gitdock.gitdocksync.service;

import edu.ehei.gitdock.gitdocksync.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdocksync.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final RabbitTemplate rabbitTemplate;

    public void processGithubPush(GithubWebhookPayload payload) {
        if (payload.getCommits() == null || payload.getCommits().isEmpty()) {
            log.info("Push vide (ex: création de branche), rien à faire.");
            return;
        }

        String repoUrl = payload.getRepository().getHtmlUrl();
        log.info("🚀 Traitement d'un Push contenant {} commits pour le repo {}", payload.getCommits().size(), repoUrl);

        // On transforme les commits GitHub en notre format interne GithubCommitResponseDTO
        List<GithubCommitResponseDTO> normalizedCommits = payload.getCommits().stream().map(webhookCommit -> {

            GithubCommitResponseDTO.Author author = new GithubCommitResponseDTO.Author();
            author.setName(webhookCommit.getAuthor().getName());
            author.setEmail(webhookCommit.getAuthor().getEmail());
            // Les dates GitHub Webhook sont en ISO 8601 avec Timezone (ex: 2024-03-22T14:30:00+01:00)
            try {
                // GitHub envoie : "2024-03-22T14:30:00+01:00" (ISO_OFFSET_DATE_TIME)
                java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(webhookCommit.getTimestamp(), java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                author.setDate(java.time.OffsetDateTime.parse(webhookCommit.getTimestamp()));
            } catch (Exception e) {
                author.setDate(java.time.OffsetDateTime.now());
            }

            GithubCommitResponseDTO.CommitDetail detail = new GithubCommitResponseDTO.CommitDetail();
            detail.setAuthor(author);
            detail.setMessage(webhookCommit.getMessage());

            GithubCommitResponseDTO dto = new GithubCommitResponseDTO();
            dto.setSha(webhookCommit.getId());
            dto.setCommit(detail);

            return dto;
        }).collect(Collectors.toList());

        // On crée un message de résultat, mais sans "projectId" car gitdock-sync ne le connaît pas !
        // C'est gitdock-project qui fera la recherche via le repoUrl.
        SyncResultMessageDTO message = new SyncResultMessageDTO();
        message.setSuccess(true);
        // On n'a pas le projectId, on passe l'URL ! (Attention, il faut ajouter ce champ dans SyncResultMessage)
        message.setRepoUrl(repoUrl);
        message.setCommits(normalizedCommits);

        // On utilise une NOUVELLE routing key spécifique pour les webhooks
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_WEBHOOK, message);
        log.info("📤 Événement Webhook envoyé à RabbitMQ pour traitement par gitdock-project");
    }
}