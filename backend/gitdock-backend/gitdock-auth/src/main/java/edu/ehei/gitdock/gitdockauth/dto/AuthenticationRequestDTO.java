package edu.ehei.gitdock.gitdockauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Objet de transfert de données (DTO) pour la requête d'authentification.
 * <p>
 * Ce DTO est utilisé lorsque l'utilisateur soumet le formulaire de connexion (Login).
 * Il contient les identifiants nécessaires pour obtenir un token JWT.
 * </p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequestDTO {

    /**
     * L'adresse email de l'utilisateur (sert d'identifiant unique).
     * Les annotations @NotBlank et @Email assurent que le champ n'est pas vide
     * et respecte le format standard d'un email avant même d'arriver au contrôleur.
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Le mot de passe en clair saisi par l'utilisateur.
     * Il sera comparé au hash stocké en base de données via l'AuthenticationManager.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}