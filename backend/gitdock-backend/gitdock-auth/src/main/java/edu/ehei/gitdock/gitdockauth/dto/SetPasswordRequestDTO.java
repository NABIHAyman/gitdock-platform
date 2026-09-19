package edu.ehei.gitdock.gitdockauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Objet de transfert de données (DTO) pour la définition initiale du mot de passe.
 * <p>
 * Ce DTO est utilisé lors de l'activation du compte (via le lien email).
 * Contrairement au changement de mot de passe (Reset), ici l'utilisateur n'a pas
 * d'ancien mot de passe à fournir.
 * </p>
 */
@Data
public class SetPasswordRequestDTO {

    /**
     * Le token d'activation unique (UUID) extrait de l'URL.
     * Il est indispensable pour valider que la requête provient bien du lien envoyé par email.
     */
    @NotBlank(message = "Le token est obligatoire")
    private String token;

    /**
     * Le mot de passe choisi par l'utilisateur.
     * <p>
     * Les contraintes de taille (6-100) sont appliquées ici pour éviter
     * les mots de passe trop faibles ou les attaques par déni de service (DoS)
     * avec des chaînes trop longues.
     * </p>
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères")
    private String password;

    /**
     * La confirmation du mot de passe.
     * Doit être strictement identique au champ 'password' (vérifié dans le Service).
     */
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    @Size(min = 6, max = 100)
    private String confirmPassword;
}