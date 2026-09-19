package edu.ehei.gitdock.gitdockauth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Objet de transfert de données (DTO) pour la requête d'activation de compte.
 * <p>
 * Ce DTO est utilisé lorsque l'utilisateur clique sur le lien d'activation reçu par email
 * et remplit le formulaire pour définir son mot de passe.
 * </p>
 */
@Data
public class ActivateAccountRequestDTO {

    /**
     * Le token d'activation unique reçu dans l'URL de l'email.
     * Il permet d'identifier l'utilisateur et de vérifier que le lien est valide et non expiré.
     */
    private String token;

    /**
     * Le mot de passe choisi par l'utilisateur.
     * La validation @Size assure une complexité minimale (8 caractères).
     */
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    /**
     * La confirmation du mot de passe.
     * Ce champ sert à vérifier que l'utilisateur n'a pas fait de faute de frappe.
     * La concordance avec 'password' est vérifiée dans la couche Service (UserActivationService).
     */
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String confirmPassword;
}