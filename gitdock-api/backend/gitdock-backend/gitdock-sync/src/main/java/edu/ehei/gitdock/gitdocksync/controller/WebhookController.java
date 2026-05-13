package edu.ehei.gitdock.gitdocksync.controller;

import edu.ehei.gitdock.gitdocksync.dto.GithubWebhookPayload;
import edu.ehei.gitdock.gitdocksync.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/sync/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    // 🟢 ROUTE DE TEST (Pour vérifier que le serveur est vivant dans ton navigateur)
    @GetMapping("/test")
    public ResponseEntity<String> testWebhook() {
        return ResponseEntity.ok("Le WebhookController est bien en ligne, Commandant !");
    }

    // 🟢 LA ROUTE TROU NOIR : Accepte du texte brut, impossible de crasher
    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestBody String rawPayload) {

        log.info("📥 Webhook GitHub reçu ! Type : {}", eventType);

        try {
            if ("push".equals(eventType)) {
                // On transforme le texte brut en objet DTO nous-mêmes
                GithubWebhookPayload payload = objectMapper.readValue(rawPayload, GithubWebhookPayload.class);
                webhookService.processGithubPush(payload);
            } else if ("ping".equals(eventType)) {
                log.info("🏓 Ping reçu de GitHub. Connexion établie avec succès !");
            }
        } catch (Exception e) {
            log.error("⚠️ Erreur lors du parsing du webhook : {}", e.getMessage());
            // Même si ça crash, on renvoie 200 OK pour que GitHub arrête de pleurer
        }

        return ResponseEntity.ok("Webhook intercepté avec succès");
    }
}