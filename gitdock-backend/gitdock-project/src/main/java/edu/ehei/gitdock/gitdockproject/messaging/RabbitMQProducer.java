package edu.ehei.gitdock.gitdockproject.messaging;

import edu.ehei.gitdock.gitdockproject.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdockproject.dto.RepoInitEvent;
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

    // Assure-toi que cet exchange correspond à ta config RabbitMQ
    private static final String EXCHANGE = "gitdock.exchange";
    private static final String ROUTING_KEY = "sync.repo.init";

    public void sendRepoInitRequest(RepoInitEvent event) {
        log.info("Envoi de l'événement d'initialisation de repo pour le projet {}", event.getProjectId());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }

    public void sendNotification(Long targetUserId, String title, String message) {
        log.info("Envoi d'une notification via RabbitMQ pour l'utilisateur {}", targetUserId);

        NotificationEventDTO event = NotificationEventDTO.builder()
                .targetUserId(targetUserId)
                .type("TYPE_PROJECT_INVITATION") // On recycle ce type qui fait apparaitre une notif standard dans ton Listener
                .payload(Map.of(
                        "title", title,
                        "message", message
                ))
                .build();

        // Remplace par tes vrais EXCHANGE et ROUTING_KEY de notification (souvent "gitdock.exchange" et "notification.routing.key")
        rabbitTemplate.convertAndSend("gitdock.exchange", "notification.routing.key", event);
    }
}