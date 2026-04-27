package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.client.AuthServiceClient;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.CommitSavedEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.SyncResultMessageDTO;
import edu.ehei.gitdock.gitdockproject.dto.UserSummaryDTO;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncResultListener {

    private final ProjectRepository projectRepository;
    private final BranchRepository branchRepository;
    private final CommitRepository commitRepository;
    private final AuthServiceClient authServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYNC_RESULT)
    @Transactional
    public void receiveSyncResult(SyncResultMessageDTO result) {
        log.info("Réception du résultat de synchronisation pour le projet ID: {}", result.getProjectId());

        if (!result.isSuccess()) {
            log.error("Échec synchro projet {}: {}", result.getProjectId(), result.getErrorMessage());

            projectRepository.findById(result.getProjectId()).ifPresent(project -> {
                Map<String, String> payload = Map.of(
                        "projectName", project.getName(),
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

        Project project = projectRepository.findById(result.getProjectId())
                .orElseThrow(() -> new RuntimeException("Projet introuvable pour la synchro"));

        // Sauvegarde des Branches
        if (result.getBranches() != null) {
            result.getBranches().forEach(ghBranch -> {
                Branch branch = Branch.builder()
                        .name(ghBranch.getName())
                        .project(project)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                branchRepository.save(branch);
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
                    commitRepository.save(commit);

                    commitRepository.save(commit);

                    // 👇 NOUVEAU : On informe le reste du système ! (Gamification, Tasks...) 👇
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
        }

        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);


        // 👇 NOUVEAU : 1. Notification de SYNC_COMPLETE pour le demandeur (via un champ userId qu'il faudrait ajouter à SyncResultMessage si ce n'est pas fait)
        // rabbitTemplate.convertAndSend(... SYNC_COMPLETE ...)

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

        log.info("Synchronisation terminée et sauvegardée avec succès pour le projet {}", project.getName());
    }
}
