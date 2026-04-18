package edu.ehei.gitdock.gitdockproject.messaging;

import edu.ehei.gitdock.gitdockproject.service.interfaces.IProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletedListener {

    private final IProjectService projectService;

    // Écoute le message venant de gitdock-auth
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "project.user.deleted.queue", durable = "true"),
            exchange = @Exchange(value = "user.exchange", type = "topic"),
            key = "user.deleted"
    ))
    public void handleUserDeletedEvent(Long deletedUserId) {
        log.info("📢 Événement RabbitMQ reçu : L'utilisateur ID {} a été supprimé. Application du protocole Ghost...", deletedUserId);
        projectService.assignToGhostUser(deletedUserId);
    }
}