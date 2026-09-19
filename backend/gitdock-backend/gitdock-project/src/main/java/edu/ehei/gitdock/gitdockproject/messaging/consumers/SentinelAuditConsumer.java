package edu.ehei.gitdock.gitdockproject.messaging.consumers;

import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.SentinelAuditResultDTO;
import edu.ehei.gitdock.gitdockproject.messaging.RabbitMQProducer;
import edu.ehei.gitdock.gitdockproject.model.Project;
import edu.ehei.gitdock.gitdockproject.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentinelAuditConsumer {

    private final ProjectRepository projectRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SENTINEL_RESULT)
    public void consumeAuditResult(SentinelAuditResultDTO report) {
        log.info("🛡️ [SENTINEL] Rapport d'audit reçu pour le commit {} !", report.getCommitHash());

        try {
            // 1. Récupération du projet (Le fetcher va envoyer le nom du repo sous forme "proprietaire/repo")
            // On cherche le projet par son URL ou on utilise l'ID direct si c'est un test
            Project project = null;
            try {
                Long projectId = Long.parseLong(report.getProjectId());
                project = projectRepository.findById(projectId).orElse(null);
            } catch (NumberFormatException e) {
                // Recherche par nom ou URL pour les vrais repos GitHub
                project = projectRepository.findAll().stream()
                        .filter(p -> p.getUrl() != null && p.getUrl().contains(report.getProjectId()))
                        .findFirst()
                        .orElse(null);
            }

            if (report.isClean()) {
                log.info("✅ Commit propre pour le projet {}. Aucune action punitive requise.", report.getProjectId());
                return; // On s'arrête là, le code est sain
            }

            // 🚨 LE CODE EST VULNÉRABLE : DÉCLENCHEMENT DES PROTOCOLES DE CRISE 🚨

            // --- ACTION 1 : NOTIFICATION IN-APP ---
            String title = "🚨 VULNÉRABILITÉ DÉTECTÉE !";
            String message = "Faille trouvée dans le commit " + report.getCommitHash() + " de " + report.getAuthor() + " : " + report.getSummary();

            Long managerId = (project != null && project.getManagedById() != null) ? project.getManagedById() : 2L;
            rabbitMQProducer.publishNotification(managerId, "TYPE_SECURITY_ALERT",
                    Map.of("title", title, "message", message));

            // --- ACTION 2 : PÉNALITÉ GAMIFICATION ---
            // On envoie un événement que le service Gamification pourra intercepter pour retirer de l'XP
            // (Il faudra créer un Consumer côté .NET Gamification pour écouter cette clé)
            Map<String, Object> penaltyPayload = Map.of(
                    "userId", managerId, // Idéalement l'auteur, mais on prend le manager par défaut
                    "xpToDeduct", 50,
                    "reason", "Faille de sécurité introduite (" + report.getVulnerabilities().get(0).getType() + ")"
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "gamification.penalty.event", penaltyPayload);
            log.info("⚔️ Pénalité de 50 XP demandée pour l'introduction de la faille.");

            // --- ACTION 3 : CRÉATION DE TÂCHE (Brouillon) ---
            // Le TaskService renvoie des erreurs 500 pour l'instant.
            // On loggue l'intention pour l'activer dès que le front des Tasks est réparé.
            log.info("🎫 TODO: Appeler le TaskService pour créer le ticket : 'Fix Security Threat in " + report.getCommitHash() + "'");

        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement du rapport Sentinel : {}", e.getMessage(), e);
        }
    }
}