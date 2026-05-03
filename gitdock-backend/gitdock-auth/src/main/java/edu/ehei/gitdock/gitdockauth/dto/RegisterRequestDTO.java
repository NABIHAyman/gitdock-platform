package edu.ehei.gitdock.gitdockauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Objet de transfert de données (DTO) pour l'inscription d'un nouvel utilisateur.
 * <p>
 * Ce DTO valide les données brutes envoyées par le formulaire d'inscription du Frontend.
 * Il garantit que les informations essentielles (Nom, Prénom, Email) sont présentes
 * avant même d'atteindre la couche Service.
 * </p>
 */
@Data
public class RegisterRequestDTO {

    /**
     * Le prénom de l'utilisateur.
     * Obligatoire pour personnaliser l'interface et les emails (ex: "Bonjour Jean").
     */
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    /**
     * Le nom de famille de l'utilisateur.
     * Obligatoire pour l'identification formelle.
     */
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    /**
     * L'adresse email, qui servira d'identifiant de connexion (Login).
     * <p>
     * Elle doit être unique dans le système (vérifié par le Service) et valide
     * car un lien d'activation y sera envoyé.
     * </p>
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Le nom d'utilisateur (pseudo) souhaité.
     * <p>
     * Ce champ est **optionnel**. S'il est laissé vide par le frontend,
     * le backend générera automatiquement un username basé sur la partie locale de l'email
     * (avant le @).
     * </p>
     */
    private String username;
}