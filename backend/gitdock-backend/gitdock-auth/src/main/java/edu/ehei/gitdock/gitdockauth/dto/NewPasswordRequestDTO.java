package edu.ehei.gitdock.gitdockauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Objet de transfert de données (DTO) pour la confirmation de réinitialisation de mot de passe.
 * <p>
 * Ce DTO est envoyé par le frontend lorsque l'utilisateur soumet le formulaire
 * "Nouveau mot de passe" après avoir cliqué sur le lien reçu par email.
 * </p>
 */
@Data
public class NewPasswordRequestDTO {

    /**
     * Le token de sécurité reçu dans le lien email (souvent passé en query param puis injecté ici).
     * Il permet d'associer la requête à un utilisateur spécifique et de vérifier
     * que la demande est récente et valide via le ResetPasswordService.
     */
    @NotBlank(message = "Le token est obligatoire")
    private String token;

    /**
     * Le nouveau mot de passe choisi par l'utilisateur.
     * <p>
     * La contrainte @Size(min = 6) assure une complexité minimale.
     * Note : En production, une validation plus stricte (Majuscule, Chiffre, etc.) serait recommandée via @Pattern.
     * </p>
     */
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String newPassword;
}