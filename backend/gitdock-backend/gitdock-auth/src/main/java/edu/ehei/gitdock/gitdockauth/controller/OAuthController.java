package edu.ehei.gitdock.gitdockauth.controller;

import edu.ehei.gitdock.gitdockauth.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockauth.exception.UserNotFoundException;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import edu.ehei.gitdock.gitdockauth.service.interfaces.IOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth") // 🚨 Route adaptée pour le microservice
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final IOAuthService oAuthService;
    private final UserAccountRepository userRepository;

    @GetMapping("/authorize/{platform}")
    public ResponseEntity<Map<String, String>> authorize(@PathVariable String platform) {
        try {
            ProjectPlatform projectPlatform = ProjectPlatform.valueOf(platform.toUpperCase());
            String url = oAuthService.getAuthorizationUrl(projectPlatform);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Plateforme non supportée"));
        }
    }

    @PostMapping("/callback/{platform}")
    public ResponseEntity<?> callback(@PathVariable String platform, @RequestBody Map<String, String> payload) {
        String code = payload.get("code");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Code manquant"));
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        try {
            ProjectPlatform projectPlatform = ProjectPlatform.valueOf(platform.toUpperCase());
            oAuthService.exchangeCodeForToken(projectPlatform, code, user);
            return ResponseEntity.ok().body(Map.of("message", "Connexion " + platform + " réussie !"));
        } catch (Exception e) {
            log.error("Erreur OAuth Callback", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint INTERNE utilisé par gitdock-sync pour récupérer le token GitHub d'un user
     */
    @GetMapping("/internal/token")
    public ResponseEntity<String> getGithubToken(@RequestParam("userId") Long userId) {
        // On récupère le compte utilisateur (tu as déjà le repository injecté)
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // On appelle le service pour chercher le token
        // Note: Tu devras ajouter cette méthode dans IOAuthService et OAuthServiceImpl !
        String token = oAuthService.getAccessToken(ProjectPlatform.GITHUB, user);

        if (token == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(token);
    }
}