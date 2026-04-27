package edu.ehei.gitdock.gitdockauth.controller;

import edu.ehei.gitdock.gitdockauth.dto.NewPasswordRequestDTO;
import edu.ehei.gitdock.gitdockauth.service.interfaces.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST gérant le processus de réinitialisation de mot de passe (Mot de passe oublié).
 * <p>
 * Ce processus se déroule en deux étapes :
 * 1. Demande de réinitialisation (envoi d'un email avec lien sécurisé).
 * 2. Confirmation de la réinitialisation (définition du nouveau mot de passe via le token).
 * </p>
 */
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final ResetPasswordService resetPasswordService;

    /**
     * Étape 1 : Initie la demande de réinitialisation.
     * L'utilisateur fournit son email. Si le compte existe, un lien contenant un token
     * est envoyé à cette adresse.
     *
     * @param email L'adresse email du compte à réinitialiser.
     * @return Une réponse 200 OK (message générique pour éviter l'énumération des utilisateurs).
     */
    @PostMapping("/request")
    public ResponseEntity<String> requestReset(@RequestParam("email") String email) {
        resetPasswordService.initiate(email);
        return ResponseEntity.ok("Lien de réinitialisation envoyé.");
    }

    /**
     * Étape 2 : Finalise la réinitialisation.
     * L'utilisateur soumet son nouveau mot de passe accompagné du token reçu par email.
     *
     * @param request DTO contenant le token et le nouveau mot de passe, validé par @Valid.
     * @return Une réponse 200 OK si le changement est effectué.
     */
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmReset(@RequestBody @Valid NewPasswordRequestDTO request) {
        resetPasswordService.confirm(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Mot de passe réinitialisé.");
    }
}