package edu.ehei.gitdock.gitdockauth.controller;

import edu.ehei.gitdock.gitdockauth.dto.ActivateAccountRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.AuthenticationRequestDTO;
import edu.ehei.gitdock.gitdockauth.dto.AuthenticationResponseDTO;
import edu.ehei.gitdock.gitdockauth.dto.RegisterRequestDTO;
import edu.ehei.gitdock.gitdockauth.service.interfaces.AuthenticationService;
import edu.ehei.gitdock.gitdockauth.service.interfaces.UserActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST principal pour l'authentification.
 * <p>
 * Il expose les points d'entrée publics (accessibles sans token) pour :
 * 1. L'inscription de nouveaux utilisateurs.
 * 2. La connexion (login) pour récupérer les tokens JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserActivationService userActivationService;

    /**
     * Endpoint d'inscription (Sign Up).
     * Crée un compte utilisateur inactif et déclenche l'envoi de l'email d'activation.
     *
     * @param request DTO contenant les infos d'inscription (nom, email, etc.) validé par @Valid.
     * @return Une réponse 202 (Accepted) indiquant que la demande est prise en compte.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO request) {
        authenticationService.register(request);
        return ResponseEntity.accepted().body("Inscription réussie. Veuillez vérifier vos emails.");
    }

    /**
     * Endpoint d'authentification (Login).
     * Vérifie les identifiants et retourne les tokens JWT si valides.
     *
     * @param request DTO contenant l'email et le mot de passe.
     * @return Une réponse 200 (OK) avec les tokens (Access & Refresh) et le profil utilisateur.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@RequestBody @Valid AuthenticationRequestDTO request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}