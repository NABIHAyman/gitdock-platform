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

                // 👇 LE BLOC UNIFIÉ POUR TOUTES LES SYNCHROS (Passe TOUJOURS l'ID au Frontend)
                case "TYPE_SYNC_STARTED":
                case "TYPE_SYNC_COMPLETE":
                case "TYPE_SYNC_FAILED":
                case "TYPE_SYNC_DELAYED":
                    Long pId = event.getPayload().get("projectId") != null ? Long.valueOf(event.getPayload().get("projectId")) : null;

                    String notifTitle = "Synchronisation";
                    if ("TYPE_SYNC_STARTED".equals(event.getType())) notifTitle = "Synchronisation démarrée";
                    else if ("TYPE_SYNC_COMPLETE".equals(event.getType())) notifTitle = "Synchronisation terminée";
                    else if ("TYPE_SYNC_FAILED".equals(event.getType())) notifTitle = "Échec de la synchronisation";
                    else if ("TYPE_SYNC_DELAYED".equals(event.getType())) notifTitle = "Synchronisation longue";

                    String notifMessage = event.getPayload().get("message");
                    if (notifMessage == null) {
                        notifMessage = "Opération sur le projet " + event.getPayload().getOrDefault("projectName", "Inconnu");
                    }

                    // On utilise la méthode à 5 arguments !
                    saveInAppNotification(event.getTargetUserId(), notifTitle, notifMessage, NotificationType.valueOf(event.getType()), pId);
                    break;

                case "TYPE_PROJECT_CREATED":
                case "TYPE_PROJECT_CREATION_FAILED":
                    saveInAppNotification(event.getTargetUserId(),
                            event.getPayload().getOrDefault("title", "Création de projet"),
                            event.getPayload().getOrDefault("message", ""),
                            NotificationType.valueOf(event.getType()));
                    break;

                case "TYPE_SECURITY_ALERT":
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

    // 👇 1. L'ANCIENNE MÉTHODE (Redirige intelligemment vers la nouvelle pour éviter de dupliquer le code)
    private void saveInAppNotification(Long userId, String title, String message, NotificationType type) {
        saveInAppNotification(userId, title, message, type, null);
    }

    // 👇 2. LA NOUVELLE MÉTHODE (Avec le 5ème argument 'entityId')
    private void saveInAppNotification(Long userId, String title, String message, NotificationType type, Long entityId) {
        if (userId == null || userId == 0L) return; // Pas de notif pour le Ghost User !

        Notification notif = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .entityId(entityId) // 👈 L'ID du projet est stocké en base !
                .build();

        // 1. On sauvegarde en base
        Notification savedNotif = notificationRepository.save(notif);

        // 2. On pousse INSTANTANÉMENT dans le canal privé de l'utilisateur
        messagingTemplate.convertAndSend("/topic/notifications." + userId, savedNotif);

        log.info("🚀 Notification poussée via WebSocket à l'utilisateur {}", userId);
    }
}