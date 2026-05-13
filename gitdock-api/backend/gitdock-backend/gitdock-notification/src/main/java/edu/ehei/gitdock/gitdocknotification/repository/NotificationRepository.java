package edu.ehei.gitdock.gitdocknotification.repository;

import edu.ehei.gitdock.gitdocknotification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Récupère toutes les notifications d’un utilisateur, triées de la plus récente à la plus ancienne
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Récupère les notifications non lues d’un utilisateur
    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    // 👇 AJOUTS (Forcent la mise à jour en base)
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsReadById(Long id);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(Long userId);
}