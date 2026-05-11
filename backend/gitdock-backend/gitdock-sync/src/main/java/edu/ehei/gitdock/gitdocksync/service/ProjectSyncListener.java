package edu.ehei.gitdock.gitdocksync.service;

import edu.ehei.gitdock.gitdocksync.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdocksync.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdocksync.dto.SyncRequestMessageDTO;
import edu.ehei.gitdock.gitdocksync.dto.SyncResultDTO;
import edu.ehei.gitdock.gitdocksync.dto.SyncResultMessageDTO;
import edu.ehei.gitdock.gitdocksync.strategy.GitPlatformSyncFactory;
import edu.ehei.gitdock.gitdocksync.strategy.IGitPlatformSyncStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import edu.ehei.gitdock.gitdocksync.model.SyncHistory; // 👈 Import de l'entité
import edu.ehei.gitdock.gitdocksync.repository.SyncHistoryRepository; // 👈 Import de repo

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectSyncListener {

    private final GitPlatformSyncFactory syncFactory; // 👈 On injecte la Factory !
    private final RabbitTemplate rabbitTemplate;
    private final SyncHistoryRepository syncHistoryRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYNC_REQUEST)
    public void handleSyncRequest(SyncRequestMessageDTO request) {
        log.info("📥 Ordre de synchronisation reçu pour le projet ID: {} (Platform: {})", request.getProjectId(), request.getPlatform());

        SyncResultMessageDTO response = new SyncResultMessageDTO();
        response.setProjectId(request.getProjectId());

        try {
            // 1. La Factory nous donne le bon outil
            IGitPlatformSyncStrategy strategy = syncFactory.getStrategy(request.getPlatform());

            // 2. L'outil fait le travail
            SyncResultDTO resultDTO = strategy.fetchProjectData(request.getRepoUrl(), request.getUserId());
            syncHistoryRepository.save(SyncHistory.builder()
                    .projectId(request.getProjectId())
                    .status(resultDTO.isSuccess() ? "SUCCESS" : "FAILED")
                    .syncDate(java.time.LocalDateTime.now())
                    .build());

            // 3. On prépare la réponse
            response.setBranches(resultDTO.getBranches());
            response.setCommits(resultDTO.getCommits());
            response.setSuccess(resultDTO.isSuccess());
            response.setErrorMessage(resultDTO.getErrorMessage());
            response.setDurationSeconds(resultDTO.getDurationSeconds());

            if (resultDTO.isSuccess()) {
                sendNotificationSyncComplete(request, resultDTO);
            } else {
                sendNotificationSyncFailed(request, resultDTO.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("❌ Erreur critique d'aspiration: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
            sendNotificationSyncFailed(request, e.getMessage());
        }

        // On renvoie le résultat au Listener de gitdock-project
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_RESULT, response);
    }

    private void sendNotificationSyncComplete(SyncRequestMessageDTO request, SyncResultDTO result) {
        try {
            Map<String, String> payload = Map.of(
                    "projectName", request.getProjectName() != null ? request.getProjectName() : "Projet Inconnu",
                    "durationSeconds", String.valueOf(result.getDurationSeconds())
            );

            NotificationEventDTO notifEvent = NotificationEventDTO.builder()
                    .targetUserId(request.getUserId())
                    .type("TYPE_SYNC_COMPLETE")
                    .payload(payload)
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, notifEvent);

            // Notification de lenteur si nécessaire
            if(result.getDurationSeconds() > 10) {
                NotificationEventDTO delayEvent = NotificationEventDTO.builder()
                        .targetUserId(request.getUserId())
                        .type("TYPE_SYNC_DELAYED")
                        .payload(payload)
                        .build();
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, delayEvent);
            }

        } catch (Exception e) {
            log.error("⚠️ Impossible d'envoyer la notification", e);
        }
    }

    private void sendNotificationSyncFailed(SyncRequestMessageDTO request, String errorMessage) {
        try {
            Map<String, String> payload = Map.of(
                    "projectName", request.getProjectName() != null ? request.getProjectName() : "Projet Inconnu",
                    "errorMessage", errorMessage
            );

            NotificationEventDTO notifEvent = NotificationEventDTO.builder()
                    .targetUserId(request.getUserId())
                    .type("TYPE_SYNC_FAILED") // Tu pourras rajouter ce type plus tard dans ton enum si tu veux !
                    .payload(payload)
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, notifEvent);

        } catch (Exception e) {
            log.error("⚠️ Impossible d'envoyer la notification", e);
        }
    }
}