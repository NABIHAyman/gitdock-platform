package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.CommitSavedEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.SyncResultMessageDTO;
import edu.ehei.gitdock.gitdockproject.dto.UserSummaryDTO;
import edu.ehei.gitdock.gitdockproject.enums.ProjectStatus;
import edu.ehei.gitdock.gitdockproject.messaging.RabbitMQProducer;
import edu.ehei.gitdock.gitdockproject.model.Branch;
import edu.ehei.gitdock.gitdockproject.model.Commit;
import edu.ehei.gitdock.gitdockproject.model.Project;
import edu.ehei.gitdock.gitdockproject.repository.BranchRepository;
import edu.ehei.gitdock.gitdockproject.repository.CommitRepository;
import edu.ehei.gitdock.gitdockproject.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import edu.ehei.gitdock.gitdockproject.config.CacheConfig;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncResultListener {

    private final ProjectRepository projectRepository;
    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;
    private final AuthServiceClient authServiceClient;
    private final RabbitTemplate rabbitTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    private final RabbitMQProducer rabbitMQProducer;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYNC_RESULT)
    @Transactional
    public void receiveSyncResult(SyncResultMessageDTO result) {
        log.info("Réception du résultat de synchronisation pour le projet ID: {}", result.getProjectId());

        if (!result.isSuccess()) {
            log.error("Échec synchro projet {}: {}", result.getProjectId(), result.getErrorMessage());

            projectRepository.findById(result.getProjectId()).ifPresent(project -> {
                Map<String, String> payload = Map.of(
                        "projectName", project.getName(),
                        "projectId", String.valueOf(project.getId()),
                        "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : "Erreur inconnue"
                );

                NotificationEventDTO event = NotificationEventDTO.builder()
                        .targetUserId(project.getManagedById())
                        .type("TYPE_SYNC_FAILED")
                        .payload(payload)
                        .build();

                // TODO: Pour Gamification et Task !
                // Ces services devront déclarer une Queue et faire un Binding
                // sur la Routing Key "project.commit.saved" dans l'exchange "gitdock.exchange"
                // Sinon ce message est perdu dans le vide intersidéral de RabbitMQ !

                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, event);
            });
            return; // On arrête le traitement ici
        }

        /*
        Project project = projectRepository.findById(result.getProjectId())
                .orElseThrow(() -> new RuntimeException("Projet introuvable pour la synchro"));
        */

        Project project = projectRepository.findById(result.getProjectId()).orElse(null);
        if (project == null) {
            log.warn("⚠️ Projet ID {} introuvable (probablement supprimé). Message ignoré pour casser la boucle RabbitMQ.", result.getProjectId());
            return; // On arrête là, le message est considéré comme traité et sortira de la file !
        }

        // Sauvegarde des Branches
        if (result.getBranches() != null) {
            result.getBranches().forEach(ghBranch -> {
                String branchLockKey = "lock:branch:" + project.getId() + ":" + ghBranch.getName();
                Boolean lockAcquired = stringRedisTemplate.opsForValue().setIfAbsent(branchLockKey, "1", java.time.Duration.ofMinutes(1));

                if (Boolean.TRUE.equals(lockAcquired)) {
                    Branch existingBranch = branchRepository.findByProjectIdAndName(project.getId(), ghBranch.getName()).orElse(null);
                   LocalDateTime realDate = ghBranch.getDate() != null ?
                           ghBranch.getDate().toLocalDateTime() : LocalDateTime.now();

                    log.debug("Branche reçue: {} | Date reçue: {}", ghBranch.getName(), ghBranch.getDate());

                   if (existingBranch == null) {
                       Branch branch = Branch.builder()
                               .name(ghBranch.getName())
                               .project(project)
                               .createdAt(LocalDateTime.now())
                               .updatedAt(realDate) // VRAIE DATE
                               .build();
                       branchRepository.save(branch);
                   } else {
                       existingBranch.setUpdatedAt(realDate); // MISE À JOUR !
                       branchRepository.save(existingBranch);
                   }
                }
            });
        }

        // Sauvegarde des Commits
        // 2. IDENTIFICATION DES AUTEURS : LE FAMEUX BATCHING ! 🚀
        Map<String, Long> emailToUserIdMap = new HashMap<>();

        if (result.getCommits() != null && !result.getCommits().isEmpty()) {

            // a. Extraire la liste stricte des emails uniques de ce lot de commits
            List<String> uniqueEmails = result.getCommits().stream()
                    .map(c -> c.getCommit().getAuthor().getEmail())
                    .filter(Objects::nonNull)
                    .distinct() // Enlève les doublons
                    .collect(Collectors.toList());

            // b. Frapper l'API Auth UNE SEULE FOIS pour avoir tous les IDs
            if (!uniqueEmails.isEmpty()) {
                try {
                    List<UserSummaryDTO> matchedUsers = authServiceClient.getUsersByEmailsInternal(uniqueEmails);
                    for (UserSummaryDTO u : matchedUsers) {
                        emailToUserIdMap.put(u.getEmail(), u.getId());
                    }
                    log.info("Batching Auth réussi : {} auteurs Git rattachés à des comptes GitDock.", matchedUsers.size());
                } catch (Exception e) {
                    log.error("Le service Auth n'a pas pu identifier le lot d'auteurs. Raison : {}", e.getMessage());
                }
            }

            // 3. Sauvegarde des Commits
            result.getCommits().forEach(ghCommit -> {
                // On vérifie que le commit n'existe pas déjà pour éviter les doublons (idempotence)
                if(!commitRepository.existsByHashAndProjectId(ghCommit.getSha(), project.getId())) {

                    String message = ghCommit.getCommit().getMessage();
                    if (message != null && message.length() > 255) {
                        message = message.substring(0, 250) + "...";
                    }

                    String authorEmail = ghCommit.getCommit().getAuthor().getEmail();

                    Commit commit = Commit.builder()
                            .hash(ghCommit.getSha())
                            .message(message)
                            .authorName(ghCommit.getCommit().getAuthor().getName())
                            .authorEmail(authorEmail)
                            // On lit la map ! (Renverra null si l'auteur n'a pas de compte, ce qui est très bien)
                            .userId(emailToUserIdMap.get(authorEmail))
                            .committedAt(ghCommit.getCommit().getAuthor().getDate().toLocalDateTime())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .project(project)
                            .additions(ghCommit.getStats() != null ? ghCommit.getStats().getAdditions() : 0)
                            .deletions(ghCommit.getStats() != null ? ghCommit.getStats().getDeletions() : 0)
                            .build();

                    // APRÈS — un seul save, propre
                    Commit savedCommit = commitRepository.save(commit);

                    if (savedCommit.getUserId() != null) {
                        CommitSavedEventDTO domainEvent = CommitSavedEventDTO.builder()
                                .commitId(savedCommit.getId())
                                .hash(savedCommit.getHash())
                                .message(savedCommit.getMessage())
                                .projectId(project.getId())
                                .branchId(savedCommit.getBranch() != null ? savedCommit.getBranch().getId() : null)
                                .authorUserId(savedCommit.getUserId())
                                .additions(savedCommit.getAdditions())
                                .deletions(savedCommit.getDeletions())
                                .build();
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.EXCHANGE_NAME,
                                RabbitMQConfig.ROUTING_KEY_COMMIT_SAVED,
                                domainEvent
                        );
                    }

                }
            });
        }

        project.setStatus(ProjectStatus.ACTIVE);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        Map<String, String> payload = Map.of(
                "projectName", project.getName(),
                "projectId", String.valueOf(project.getId()),
                "message", "Le projet " + project.getName() + " est entièrement synchronisé."
        );
        rabbitMQProducer.publishNotification(project.getManagedById(), "TYPE_SYNC_COMPLETE", payload);
        log.info("🔔 Notification SYNC_COMPLETE envoyée au manager du projet.");
        /*
        try {
            Map<String, String> syncPayload = Map.of(
                    "projectName", project.getName(),
                    "commitCount", String.valueOf(result.getCommits() != null ? result.getCommits().size() : 0)
            );

            NotificationEventDTO syncEvent = NotificationEventDTO.builder()
                    .targetUserId(project.getManagedById()) // On notifie le chef de projet
                    .type("TYPE_SYNC_COMPLETE")
                    .payload(syncPayload)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_NOTIFICATION,
                    syncEvent
            );
            log.info("🔔 Notification SYNC_COMPLETE envoyée au manager du projet.");
        } catch (Exception e) {
            log.error("⚠️ Impossible d'envoyer la notification SYNC_COMPLETE : {}", e.getMessage());
        }
        */
        /*
        // 👇 NOUVEAU : 2. Fan-out des notifications NEW_COMMIT aux membres du projet
        if (result.getCommits() != null && !result.getCommits().isEmpty()) {
            List<Long> memberIds = project.getUserProjects().stream()
                    .map(up -> up.getUserId())
                    .collect(Collectors.toList());

            // Pour chaque commit ramené par la synchro
            result.getCommits().forEach(ghCommit -> {
                String authorEmail = ghCommit.getCommit().getAuthor().getEmail();
                Long authorUserId = emailToUserIdMap.get(authorEmail);

                // On notifie tout le monde SAUF l'auteur du commit
                for (Long memberId : memberIds) {
                    if (!memberId.equals(authorUserId)) {
                        java.util.Map<String, String> payload = java.util.Map.of(
                                "projectName", project.getName(),
                                "authorName", ghCommit.getCommit().getAuthor().getName(),
                                "commitMessage", ghCommit.getCommit().getMessage()
                        );

                        NotificationEventDTO event = NotificationEventDTO.builder()
                                .targetUserId(memberId)
                                .type("TYPE_NEW_COMMIT")
                                .payload(payload)
                                .build();

                        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "notification.routing.key", event);
                    }
                }
            });
        }
         */

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

        log.info("Synchronisation terminée et sauvegardée avec succès pour le projet {}", project.getName());
    }
}
