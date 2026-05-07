package edu.ehei.gitdock.gitdocksync.controller;

import edu.ehei.gitdock.gitdocksync.dto.GithubWebhookPayload;
import edu.ehei.gitdock.gitdocksync.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestBody GithubWebhookPayload payload) {

        log.info("📥 Webhook GitHub reçu ! Type d'événement : {}", eventType);

        // On ne traite que les événements de type "push" pour l'instant
        if ("push".equals(eventType)) {
            webhookService.processGithubPush(payload);
        }

        return ResponseEntity.ok("Webhook reçu et traité");
    }
}