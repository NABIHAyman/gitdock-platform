package edu.ehei.gitdock.gitdockproject.messaging;

import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.CommitSavedEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.SyncRequestMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import edu.ehei.gitdock.gitdockproject.dto.RepoInitEvent;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    // Assure-toi que cet exchange correspond à ta config RabbitMQ
    private static final String EXCHANGE = "gitdock.exchange";
    private static final String ROUTING_KEY = "sync.repo.init";

    /**
     * RÉSOUT L'ERREUR : Cette méthode manquait pour l'étape 3 de ta Saga.
     * Utilise ROUTING_KEY_REQUEST ("sync.request") de ta config.
     */
    // 👇 2. LE FIX EST ICI : On change le type du paramètre
    public void sendRepoInitRequest(SyncRequestMessageDTO event) {
        log.info("SAGA - Étape 3 : Envoi de la demande d'initialisation pour le projet {}", event.getProjectId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_REQUEST,
                event
        );
    }

    /**
     * Utilise ROUTING_KEY_NOTIFICATION de ta config.
     */
    public void sendNotification(Long targetUserId, String title, String message) {
        log.info("Envoi d'une notification pour l'utilisateur {}", targetUserId);
        NotificationEventDTO event = NotificationEventDTO.builder()
                .targetUserId(targetUserId)
                .type("TYPE_PROJECT_INVITATION")
                .payload(Map.of("title", title, "message", message))
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_NOTIFICATION,
                event
        );
    }
    
    /**
     * Utilise ROUTING_KEY_COMMIT_SAVED de ta config.
     */
    public void sendCommitSavedEvent(CommitSavedEventDTO event) {
        log.info("Envoi de l'événement commit pour l'utilisateur {}", event.getAuthorUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_COMMIT_SAVED,
                event
        );
    }
}