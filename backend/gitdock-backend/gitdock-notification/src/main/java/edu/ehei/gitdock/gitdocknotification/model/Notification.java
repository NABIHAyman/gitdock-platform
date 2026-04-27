package edu.ehei.gitdock.gitdocknotification.model;

import edu.ehei.gitdock.gitdocknotification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🟢 Remplacé : Plus de @ManyToOne UserAccount
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 🟢 Simplifié : Gardons ça pour savoir si c'est lié à un Projet, un Commit ou un Badge
    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    // 🟢 Remplacé : L'ID de celui qui a déclenché l'action (ex: L'admin qui invite)
    @Column(name = "triggered_by_user_id")
    private Long triggeredByUserId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}