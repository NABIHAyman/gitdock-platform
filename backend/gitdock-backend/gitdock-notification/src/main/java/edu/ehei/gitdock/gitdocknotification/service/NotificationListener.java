package edu.ehei.gitdock.gitdocknotification.service;

import edu.ehei.gitdock.gitdocknotification.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdocknotification.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdocknotification.enums.NotificationType;
import edu.ehei.gitdock.gitdocknotification.model.Notification;
import edu.ehei.gitdock.gitdocknotification.repository.NotificationRepository;
import edu.ehei.gitdock.gitdocknotification.service.interfaces.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailSenderService emailSenderService;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION)
    public void processNotificationEvent(NotificationEventDTO event) {
        log.info("🔔 Événement reçu : {} pour {}", event.getType(), event.getTargetEmail() != null ? event.getTargetEmail() : "User " + event.getTargetUserId());

        try {
            switch (event.getType()) {
                case "TYPE_USER_REGISTERED":
                    emailSenderService.sendActivationEmail(
                            event.getTargetEmail(),
                            event.getPayload().get("firstName"),
                            event.getPayload().get("lastName"),
                            event.getPayload().get("activationLink")
                    );
                    break;

                case "TYPE_PASSWORD_RESET_REQUESTED":
                    emailSenderService.sendPasswordResetEmail(
                            event.getTargetEmail(),
                            event.getPayload().get("firstName"),
                            event.getPayload().get("lastName"),
                            event.getPayload().get("resetLink")
                    );
                    break;

                case "TYPE_PROJECT_INVITATION":
                    // 1. Save In-App
                    saveInAppNotification(event.getTargetUserId(),
                            "Nouveau Projet",
                            "Vous avez été ajouté(e) au projet : " + event.getPayload().get("projectName"),
                            NotificationType.TYPE_PROJECT_INVITATION);

                    // 2. Send Email (si lien présent, c'est un invité non-inscrit)
                    if (event.getPayload().containsKey("invitationLink")) {
                        emailSenderService.sendInvitationEmail(
                                event.getTargetEmail(),
                                event.getPayload().get("firstName"),
                                event.getPayload().get("lastName"),
                                event.getPayload().get("invitationLink"),
                                event.getPayload().get("projectName")
                        );
                    }
                    break;

                case "TYPE_NEW_COMMIT":
                    saveInAppNotification(event.getTargetUserId(),
                            "Nouveau Commit - " + event.getPayload().get("projectName"),
                            event.getPayload().get("authorName") + " : " + event.getPayload().get("commitMessage"),
                            NotificationType.TYPE_NEW_COMMIT);
                    break;

                case "TYPE_SYNC_DELAYED":
                    saveInAppNotification(event.getTargetUserId(),
                            "Synchronisation longue",
                            "La synchro du projet " + event.getPayload().get("projectName") + " prend du temps (" + event.getPayload().get("durationSeconds") + "s).",
                            NotificationType.TYPE_SYNC_DELAYED);
                    break;

                case "TYPE_SYNC_COMPLETE":
                    saveInAppNotification(event.getTargetUserId(),
                            "Synchronisation terminée",
                            "Le projet " + event.getPayload().get("projectName") + " est à jour !",
                            NotificationType.TYPE_SYNC_COMPLETE);
                    break;

                case "TYPE_SYNC_FAILED":
                    saveInAppNotification(event.getTargetUserId(),
                            "Échec de la synchronisation",
                            "L'aspirateur n'a pas pu accéder au projet " + event.getPayload().get("projectName") + ". " +
                                    "Si c'est un dépôt privé, assurez-vous d'avoir lié votre compte dans votre Profil.",
                            NotificationType.TYPE_SYNC_FAILED);
                    break;

                case "TYPE_TASK_ASSIGNED":
                case "TYPE_DEADLINE_REMINDER":
                case "TYPE_BADGE_EARNED":
                case "TYPE_LEVEL_ACHIEVED":
                    saveInAppNotification(event.getTargetUserId(),
                            event.getPayload().getOrDefault("title", "Nouvelle notification"),
                            event.getPayload().getOrDefault("message", "Consultez votre espace pour plus de détails."),
                            NotificationType.valueOf(event.getType()));
                    break;

                default:
                    log.warn("⚠️ Type d'événement inconnu : {}", event.getType());
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de la notification : {}", e.getMessage(), e);
        }
    }

    private void saveInAppNotification(Long userId, String title, String message, NotificationType type) {
        if (userId == null || userId == 0L) return; // Pas de notif pour le Ghost User !

        Notification notif = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .build();

        // 1. On sauvegarde en base
        Notification savedNotif = notificationRepository.save(notif);

        // 2. On pousse INSTANTANÉMENT dans le canal privé de l'utilisateur
        // L'URL d'abonnement sera par exemple : /topic/notifications.2 (pour le userId 2)
        messagingTemplate.convertAndSend("/topic/notifications." + userId, savedNotif);

        log.info("🚀 Notification poussée via WebSocket à l'utilisateur {}", userId);
    }
}