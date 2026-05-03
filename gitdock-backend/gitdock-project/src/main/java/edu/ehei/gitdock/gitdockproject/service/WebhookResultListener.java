package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.CommitSavedEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.SyncResultMessageDTO;
import edu.ehei.gitdock.gitdockproject.dto.UserSummaryDTO;
import edu.ehei.gitdock.gitdockproject.model.Commit;
import edu.ehei.gitdock.gitdockproject.model.Project;
import edu.ehei.gitdock.gitdockproject.repository.CommitRepository;
import edu.ehei.gitdock.gitdockproject.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookResultListener {

    private final ProjectRepository projectRepository;
    private final CommitRepository commitRepository;
    private final AuthServiceClient authServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_WEBHOOK_RESULT)
    @Transactional
    public void receiveWebhookResult(SyncResultMessageDTO result) {
        log.info("🎯 Webhook intercepté par gitdock-project pour l'URL : {}", result.getRepoUrl());

        if (result.getRepoUrl() == null || result.getCommits() == null || result.getCommits().isEmpty()) {
            return;
        }

        // 1. Trouver le ou les projets correspondants à cette URL
        // On enlève "https://" et ".git" pour faire une recherche large
        String cleanUrl = result.getRepoUrl().replace("https://", "").replace("http://", "").replace(".git", "");
        List<Project> matchingProjects = projectRepository.findByUrlContainingIgnoreCase(cleanUrl);

        if (matchingProjects.isEmpty()) {
            log.warn("⚠️ Aucun projet GitDock ne correspond à l'URL du Webhook : {}", cleanUrl);
            return;
        }

        // 2. Identifier les auteurs (Batching avec Auth Service)
        Map<String, Long> emailToUserIdMap = new HashMap<>();
        List<String> uniqueEmails = result.getCommits().stream()
                .map(c -> c.getCommit().getAuthor().getEmail())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (!uniqueEmails.isEmpty()) {
            try {
                List<UserSummaryDTO> matchedUsers = authServiceClient.getUsersByEmailsInternal(uniqueEmails);
                for (UserSummaryDTO u : matchedUsers) {
                    emailToUserIdMap.put(u.getEmail(), u.getId());
                }
            } catch (Exception e) {
                log.error("Erreur lors de l'identification des auteurs du webhook", e);
            }
        }

        // 3. Sauvegarder les commits et notifier pour CHAQUE projet concerné
        for (Project project : matchingProjects) {

            // Extraire tous les membres du projet pour le Fan-out des notifications
            List<Long> memberIds = project.getUserProjects().stream()
                    .map(up -> up.getUserId())
                    .collect(Collectors.toList());

            result.getCommits().forEach(ghCommit -> {
                // Sauvegarde du commit s'il n'existe pas déjà
                if (!commitRepository.existsByHashAndProjectId(ghCommit.getSha(), project.getId())) {

                    String message = ghCommit.getCommit().getMessage();
                    if (message != null && message.length() > 255) {
                        message = message.substring(0, 250) + "...";
                    }

                    String authorEmail = ghCommit.getCommit().getAuthor().getEmail();
                    Long authorUserId = emailToUserIdMap.get(authorEmail);

                    Commit commit = Commit.builder()
                            .hash(ghCommit.getSha())
                            .message(message)
                            .authorName(ghCommit.getCommit().getAuthor().getName())
                            .authorEmail(authorEmail)
                            .userId(authorUserId)
                            .committedAt(ghCommit.getCommit().getAuthor().getDate().toLocalDateTime())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .project(project)
                            .build();
                    commitRepository.save(commit);

                    // 🔔 NOTIFICATION TEMPS RÉEL AUX MEMBRES !
                    for (Long memberId : memberIds) {
                        // On ne notifie pas le gars qui vient de faire le Push !
                        if (!memberId.equals(authorUserId)) {
                            Map<String, String> payload = Map.of(
                                    "projectName", project.getName(),
                                    "authorName", commit.getAuthorName(),
                                    "commitMessage", commit.getMessage()
                            );

                            NotificationEventDTO event = NotificationEventDTO.builder()
                                    .targetUserId(memberId)
                                    .type("TYPE_NEW_COMMIT")
                                    .payload(payload)
                                    .build();

                            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, event);
                        }
                    }

                    commitRepository.save(commit);

                    // 👇 On informe le reste du système ! (Gamification, Tasks...) 👇
                    if (commit.getUserId() != null) {
                        CommitSavedEventDTO domainEvent = CommitSavedEventDTO.builder()
                                .commitId(commit.getId())
                                .hash(commit.getHash())
                                .message(commit.getMessage())
                                .projectId(project.getId())
                                .branchId(commit.getBranch() != null ? commit.getBranch().getId() : null)
                                .authorUserId(commit.getUserId())
                                .additions(commit.getAdditions())
                                .deletions(commit.getDeletions())
                                .build();

                        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_COMMIT_SAVED, domainEvent);
                    }
                }
            });

            // Met à jour la date de dernière activité du projet
            project.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project);
            log.info("✅ {} nouveaux commits ajoutés au projet '{}'", result.getCommits().size(), project.getName());
        }
    }
}