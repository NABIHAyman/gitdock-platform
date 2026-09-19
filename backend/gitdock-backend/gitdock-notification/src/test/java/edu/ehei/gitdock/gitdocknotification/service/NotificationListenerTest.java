package edu.ehei.gitdock.gitdocknotification.service;

import edu.ehei.gitdock.gitdocknotification.dto.NotificationEventDTO;
import edu.ehei.gitdock.gitdocknotification.model.Notification;
import edu.ehei.gitdock.gitdocknotification.repository.NotificationRepository;
import edu.ehei.gitdock.gitdocknotification.service.interfaces.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe de test d'intégration pour le service de notification.
 * L'annotation @ActiveProfiles("test") est celle que j'ai ajoutée pour
 * utiliser la configuration de test et réussir l'exécution.
 */
@SpringBootTest
@ActiveProfiles("test")
public class NotificationListenerTest {

    @Autowired
    private NotificationListener notificationListener;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private EmailSenderService emailSenderService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    /**
     * TEST 1 : Inscription d'un utilisateur (Réussi)
     * Vérifie que le système envoie bien l'email d'activation.
     */
    @Test
    void processNotificationEvent_ShouldCallSendActivationEmail_WhenUserRegistered() {
//l'événement RabbitMQ de type TYPE_USER_REGISTERED
//La vérification que la fonction d'envoi d'email (sendActivationEmail) a été déclenchée
        // 1. PRÉPARATION DES DONNÉES
        Map<String, String> payload = new HashMap<>();
        payload.put("firstName", "Amal");
        payload.put("lastName", "Dev");
        payload.put("activationLink", "http://gitdock.com/activate/12345");

        NotificationEventDTO mockEvent = NotificationEventDTO.builder()
                .targetEmail("amal.dev@test.com")
                .type("TYPE_USER_REGISTERED")
                .payload(payload)
                .build();

        // 2. EXÉCUTION
        notificationListener.processNotificationEvent(mockEvent);

        // 3. VÉRIFICATION
        // On vérifie que le service d'email a été appelé exactement 1 fois avec les bons paramètres
        Mockito.verify(emailSenderService, Mockito.times(1)).sendActivationEmail(
                "amal.dev@test.com",
                "Amal",
                "Dev",
                "http://gitdock.com/activate/12345"
        );

        // Bonus : On vérifie qu'aucune notification n'a été créée en base pour ce cas
        Mockito.verifyNoInteractions(notificationRepository, messagingTemplate);
    }
    /**
     * TEST 2 : Invitation à un projet
     * Ce test vérifie que lorsqu'un utilisateur est invité à un projet, l'information
     * se divise correctement en 3 flux :
     * 1. Sauvegarde en base de données (In-App)
     * 2. Envoi en temps réel via WebSocket
     * 3. Envoi d'un email d'invitation (car le lien d'invitation est présent)
     */
    @Test
    void processNotificationEvent_ShouldSaveInAppAndSendEmail_WhenProjectInvitation() {

        // -----------------------------------------------------------------
        // 1. PRÉPARATION (ARRANGE) : On prépare nos données fictives
        // -----------------------------------------------------------------
        Long targetUserId = 42L;

        // On prépare le dictionnaire (payload) avec les infos du projet et le lien
        Map<String, String> payload = new HashMap<>();
        payload.put("projectName", "GitDock-Super-Project");
        payload.put("firstName", "Amal");
        payload.put("lastName", "Dev");
        // La présence de cette clé "invitationLink" est ce qui déclenche l'email dans ton code !
        payload.put("invitationLink", "http://gitdock.com/invite/999");

        // On construit l'événement déclencheur simulé
        NotificationEventDTO mockEvent = NotificationEventDTO.builder()
                .targetUserId(targetUserId)
                .targetEmail("amal.dev@test.com")
                .type("TYPE_PROJECT_INVITATION") // C'est CE type qui déclenche la logique
                .payload(payload)
                .build();

        // IMPORTANT : SIMULATION DU REPOSITORY
        // Dans mon code réel, le listener sauvegarde l'objet, puis envoie le résultat au WebSocket.
        // Puisque notre Repository est un Mock (une coquille vide), on doit lui dicter son comportement :
        // "Quand on appelle ta méthode save(), retourne cet objet mockSavedNotification".
        Notification mockSavedNotification = Notification.builder()
                .userId(targetUserId)
                .title("Nouveau Projet")
                .message("Vous avez été ajouté(e) au projet : GitDock-Super-Project")
                .type(edu.ehei.gitdock.gitdocknotification.enums.NotificationType.TYPE_PROJECT_INVITATION)
                .build();
        Mockito.when(notificationRepository.save(Mockito.any(Notification.class))).thenReturn(mockSavedNotification);

        // -----------------------------------------------------------------
        // 2. EXÉCUTION (ACT) : On lance la machine
        // -----------------------------------------------------------------
        notificationListener.processNotificationEvent(mockEvent);

        // -----------------------------------------------------------------
        // 3. VÉRIFICATION (ASSERT / VERIFY) : On s'assure que tout s'est bien passé
        // -----------------------------------------------------------------

        // Flux 1 : On vérifie que la logique de sauvegarde en base a bien été appelée 1 fois
        Mockito.verify(notificationRepository, Mockito.times(1)).save(Mockito.any(Notification.class));

        // Flux 2 : On vérifie que l'information a bien été envoyée au WebSocket sur le bon canal (userId 42)
        Mockito.verify(messagingTemplate, Mockito.times(1)).convertAndSend(
                Mockito.eq("/topic/notifications." + targetUserId),
                Mockito.eq(mockSavedNotification)
        );

        // Flux 3 : On vérifie que l'email d'invitation a bien été envoyé avec les bonnes données
        Mockito.verify(emailSenderService, Mockito.times(1)).sendInvitationEmail(
                "amal.dev@test.com",
                "Amal",
                "Dev",
                "http://gitdock.com/invite/999",
                "GitDock-Super-Project"
        );
    }
    /**
     * TEST 3 : Demande de réinitialisation de mot de passe
     * Vérifie le flux unique de l'envoi d'email de reset.
     */
    @Test
    void processNotificationEvent_ShouldSendResetEmail_WhenPasswordResetRequested() {
        // 1. PRÉPARATION
        Map<String, String> payload = new HashMap<>();
        payload.put("firstName", "Amal");
        payload.put("lastName", "Dev");
        payload.put("resetLink", "http://gitdock.com/reset/777");

        NotificationEventDTO mockEvent = NotificationEventDTO.builder()
                .targetEmail("amal.dev@test.com")
                .type("TYPE_PASSWORD_RESET_REQUESTED") // Déclencheur du reset
                .payload(payload)
                .build();

        // 2. EXÉCUTION
        notificationListener.processNotificationEvent(mockEvent);

        // 3. VÉRIFICATION
        // L'email doit être envoyé
        Mockito.verify(emailSenderService, Mockito.times(1)).sendPasswordResetEmail(
                "amal.dev@test.com", "Amal", "Dev", "http://gitdock.com/reset/777"
        );
        // AUCUNE interaction avec la base de données ou le WebSocket n'est tolérée ici !
        Mockito.verifyNoInteractions(notificationRepository, messagingTemplate);
    }

    /**
     * TEST 4 : Nouveau Commit
     * Vérifie que l'alerte In-App fonctionne, mais qu'AUCUN email n'est envoyé (pour ne pas spammer).
     */
    @Test
    void processNotificationEvent_ShouldSaveInAppOnly_WhenNewCommit() {
        // 1. PRÉPARATION
        Long targetUserId = 42L;
        Map<String, String> payload = new HashMap<>();
        payload.put("projectName", "GitDock-Backend");
        payload.put("authorName", "Amal");
        payload.put("commitMessage", "Fix bug in listener");

        NotificationEventDTO mockEvent = NotificationEventDTO.builder()
                .targetUserId(targetUserId)
                .type("TYPE_NEW_COMMIT") // Déclencheur du commit
                .payload(payload)
                .build();

        Notification mockSavedNotification = Notification.builder()
                .userId(targetUserId)
                .title("Nouveau Commit - GitDock-Backend")
                .message("Amal : Fix bug in listener")
                .type(edu.ehei.gitdock.gitdocknotification.enums.NotificationType.TYPE_NEW_COMMIT)
                .build();

        Mockito.when(notificationRepository.save(Mockito.any(Notification.class))).thenReturn(mockSavedNotification);

        // 2. EXÉCUTION
        notificationListener.processNotificationEvent(mockEvent);

        // 3. VÉRIFICATION
        // La base de données et le WebSocket doivent être sollicités
        Mockito.verify(notificationRepository, Mockito.times(1)).save(Mockito.any(Notification.class));
        Mockito.verify(messagingTemplate, Mockito.times(1)).convertAndSend(
                Mockito.eq("/topic/notifications." + targetUserId),
                Mockito.eq(mockSavedNotification)
        );

        // VÉRIFICATION CRUCIALE : Le service d'email NE DOIT PAS être appelé.
        // j'ai testé que mon appli ne spamme pas les utilisateurs !
        Mockito.verifyNoInteractions(emailSenderService);
    }
}