package edu.ehei.gitdock.gitdocknotification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDTO {
    private Long targetUserId;       // ID de l'utilisateur (0L si Inconnu/Ghost)
    private String targetEmail;      // L'email (vital pour les nouveaux invités qui n'ont pas encore de vrai compte)
    private String type;             // ex: "USER_REGISTERED", "PROJECT_INVITATION", "BADGE_EARNED"

    // Un dictionnaire fourre-tout pour passer les variables du template (Prénom, Liens, Nom du projet, etc.)
    private Map<String, String> payload;
}