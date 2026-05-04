package edu.ehei.gitdock.gitdockproject.messaging;

import edu.ehei.gitdock.gitdockproject.dto.CommitSavedEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.NotificationEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "gitdock.exchange";
    private static final String ROUTING_KEY_COMMIT = "commit.saved.event";
    private static final String ROUTING_KEY_NOTIFICATION = "notification.routing.key";

    public void sendCommitSavedEvent(CommitSavedEventDTO event) {
        log.info("Envoi de l'événement commit pour l'utilisateur {}", event.getAuthorUserId());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_COMMIT, event);
    }

    public void sendNotification(Long targetUserId, String title, String message) {
        log.info("Envoi d'une notification pour l'utilisateur {}", targetUserId);
        NotificationEventDTO event = NotificationEventDTO.builder()
                .targetUserId(targetUserId)
                .type("TYPE_PROJECT_INVITATION")
                .payload(Map.of("title", title, "message", message))
                .build();
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_NOTIFICATION, event);
    }
}