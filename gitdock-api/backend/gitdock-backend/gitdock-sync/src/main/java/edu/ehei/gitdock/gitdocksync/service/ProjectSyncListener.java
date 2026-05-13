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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectSyncListener {

    private final GitPlatformSyncFactory syncFactory; // 👈 On injecte la Factory !
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYNC_REQUEST)
    public void handleSyncRequest(SyncRequestMessageDTO request) {
        log.info("📥 Ordre de synchronisation reçu pour le projet ID: {} (Platform: {})", request.getProjectId(), request.getPlatform());

        sendNotificationSyncStarted(request);

        SyncResultMessageDTO response = new SyncResultMessageDTO();
        response.setProjectId(request.getProjectId());

        try {
            // 1. La Factory nous donne le bon outil
            IGitPlatformSyncStrategy strategy = syncFactory.getStrategy(request.getPlatform());

            // 2. L'outil fait le travail
            SyncResultDTO resultDTO = strategy.fetchProjectData(request.getRepoUrl(), request.getUserId());

            // 3. On prépare la réponse
            response.setBranches(resultDTO.getBranches());
            response.setCommits(resultDTO.getCommits());
            response.setSuccess(resultDTO.isSuccess());
            response.setErrorMessage(resultDTO.getErrorMessage());
            response.setDurationSeconds(resultDTO.getDurationSeconds());

        } catch (Exception e) {
            log.error("❌ Erreur critique d'aspiration: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
        }

        // On renvoie le résultat au Listener de gitdock-project
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_RESULT, response);
    }

    private void sendNotificationSyncStarted(SyncRequestMessageDTO request) {
        try {
            Map<String, String> payload = Map.of(
                    "projectName", request.getProjectName() != null ? request.getProjectName() : "Projet Inconnu"
            );
            NotificationEventDTO notifEvent = NotificationEventDTO.builder()
                    .targetUserId(request.getUserId())
                    .type("TYPE_SYNC_STARTED")
                    .payload(payload)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_NOTIFICATION, notifEvent);
        } catch (Exception e) {
            log.error("⚠️ Impossible d'envoyer la notification de démarrage", e);
        }
    }
}