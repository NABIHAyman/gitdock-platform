package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.*;
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
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import edu.ehei.gitdock.gitdockproject.config.CacheConfig;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookResultListener {

    private final ProjectRepository projectRepository;
    private final CommitRepository commitRepository;
    private final AuthServiceClient authServiceClient;
    private final RabbitTemplate rabbitTemplate;

    private final StringRedisTemplate stringRedisTemplate;

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
                // 👇 LE VERROU DISTRIBUÉ (ANTI-CRASH ABSOLU)
                String commitLockKey = "lock:commit:" + project.getId() + ":" + ghCommit.getSha();
                Boolean lockAcquired = stringRedisTemplate.opsForValue().setIfAbsent(commitLockKey, "1", java.time.Duration.ofMinutes(1));

                if (Boolean.TRUE.equals(lockAcquired)) {
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

                        // 1. On sauvegarde le commit en premier
                        commitRepository.save(commit);

                        // 2. 🔔 NOTIFICATION TEMPS RÉEL AUX MEMBRES !
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

                        // 3. 🚀 DEMANDE D'ENRICHISSEMENT (Gamification & Sécurité)
                        CommitEnrichmentRequestDTO enrichReq = CommitEnrichmentRequestDTO.builder()
                                .projectId(project.getId())
                                .commitSha(ghCommit.getSha())
                                .repoUrl(project.getUrl())
                                .managerId(project.getManagedById())
                                .authorUserId(authorUserId)
                                .build();

                        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_ENRICH_REQUEST, enrichReq);

                    }
                }
            });

            // Met à jour la date de dernière activité du projet
            project.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project);

            // 👇 LA FRAPPE D'INVALIDATION OPTIMISÉE (Cache Redis par SCAN et Batch) 👇
            try {
                List<String> keysToDelete = new ArrayList<>();

                // 1. Scan des clés pour les Commits
                org.springframework.data.redis.core.ScanOptions optionsCommits = org.springframework.data.redis.core.ScanOptions.scanOptions()
                        .match(CacheConfig.CACHE_COMMITS + "::" + project.getId() + "*")
                        .count(100).build();

                try (org.springframework.data.redis.core.Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory().getConnection().scan(optionsCommits)) {
                    while (cursor.hasNext()) {
                        keysToDelete.add(new String(cursor.next()));
                    }
                }

                // 2. Scan des clés pour les Branches
                org.springframework.data.redis.core.ScanOptions optionsBranches = org.springframework.data.redis.core.ScanOptions.scanOptions()
                        .match(CacheConfig.CACHE_BRANCHES + "::" + project.getId() + "*")
                        .count(100).build();

                try (org.springframework.data.redis.core.Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory().getConnection().scan(optionsBranches)) {
                    while (cursor.hasNext()) {
                        keysToDelete.add(new String(cursor.next()));
                    }
                }

                // 3. Suppression groupée (Un seul appel réseau vers Redis !)
                if (!keysToDelete.isEmpty()) {
                    stringRedisTemplate.delete(keysToDelete);
                    log.info("🧹 Cache Redis purgé proprement ({} clés supprimées en Batch) pour le projet {}", keysToDelete.size(), project.getName());
                }

            } catch (Exception e) {
                log.warn("⚠️ Impossible de vider le cache Redis pour le projet {} : {}", project.getId(), e.getMessage());
            }

            log.info("✅ {} nouveaux commits ajoutés au projet '{}'", result.getCommits().size(), project.getName());
        }
    }
}