package edu.ehei.gitdock.gitdockauth.controller;

import edu.ehei.gitdock.gitdockauth.dto.SetPasswordRequestDTO;
import edu.ehei.gitdock.gitdockauth.service.interfaces.UserActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST gérant le processus d'activation de compte par email.
 * <p>
 * Ce contrôleur expose les endpoints nécessaires pour :
 * 1. Valider un token d'activation (GET).
 * 2. Finaliser l'activation en définissant le mot de passe (POST).
 * 3. Renvoyer un email d'activation si le précédent a expiré (POST).
 * </p>
 */
@RestController
@RequestMapping("/api/auth/user-activation")
@RequiredArgsConstructor
public class ActivationController {

    private final UserActivationService userActivationService;

    /**
     * Finalise l'activation du compte.
     * Cette méthode est appelée lorsque l'utilisateur soumet son mot de passe
     * sur la page d'activation.
     *
     * @param request DTO contenant le token et le mot de passe choisi.
     * @return Une réponse 200 OK si l'activation réussit.
     */
    @PostMapping("/confirm")
    public ResponseEntity<String> activateAccount(@RequestBody @Valid SetPasswordRequestDTO request) {
        userActivationService.activateUser(request);
        return ResponseEntity.ok("Compte activé avec succès !");
    }

    /**
     * Renvoie un email d'activation.
     * Utile si le token a expiré ou si l'utilisateur a perdu l'email initial.
     *
     * @param email L'adresse email de l'utilisateur.
     * @return Une réponse de confirmation.
     */
    @PostMapping("/resend")
    public ResponseEntity<String> resendActivationEmail(@RequestParam("email") String email) {
        userActivationService.resendActivationEmail(email);
        return ResponseEntity.ok("Nouveau lien d'activation envoyé.");
    }

    /**
     * Vérifie la validité d'un token d'activation.
     * Généralement appelé par le Frontend dès le chargement de la page d'activation
     * pour savoir s'il faut afficher le formulaire de mot de passe ou un message d'erreur.
     *
     * @param token Le token extrait de l'URL.
     * @return true si le token est valide, false sinon.
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam("token") String token) {
        return ResponseEntity.ok(userActivationService.isValidActivationToken(token));
    }
}