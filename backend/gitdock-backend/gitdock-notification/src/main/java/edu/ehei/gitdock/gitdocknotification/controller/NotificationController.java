package edu.ehei.gitdock.gitdocknotification.controller;

import edu.ehei.gitdock.gitdocknotification.model.Notification;
import edu.ehei.gitdock.gitdocknotification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // 1. Lire les notifications d'un utilisateur
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(@RequestHeader("X-User-Id") Long userId) {
        // Idéalement, tu extrais l'ID du token JWT, ici on le passe en header pour simplifier l'exemple
        log.info("🔔 Récupération des notifications pour userId = {}", userId);

        List<Notification> notifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        log.info("📦 Nombre de notifications trouvées : {}", notifs.size());

        return ResponseEntity.ok(notifs);
    }

    // 2. Marquer une notification comme lue
    @PatchMapping("/{id}/read")
    @Transactional
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(notif -> {
            notif.setRead(true);
            notificationRepository.save(notif);
        });
        return ResponseEntity.ok().build();
    }

    // 3. Tout marquer comme lu
    @PatchMapping("/read-all")
    @Transactional
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok().build();
    }
}