package edu.ehei.gitdock.gitdockproject.service.UsesCases;

import edu.ehei.gitdock.gitdockproject.dto.ProjectResponseDTO;
import edu.ehei.gitdock.gitdockproject.dto.RepoInitEvent;
import edu.ehei.gitdock.gitdockproject.dto.SagaProjectCreationRequestDTO;
import edu.ehei.gitdock.gitdockproject.enums.ProjectStatus;
import edu.ehei.gitdock.gitdockproject.exception.SagaExecutionException;
import edu.ehei.gitdock.gitdockproject.mapper.ProjectMapper;
import edu.ehei.gitdock.gitdockproject.messaging.RabbitMQProducer;
import edu.ehei.gitdock.gitdockproject.model.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSagaOrchestrator {

    // On injecte les services locaux et les clients Feign
    private final ProjectLocalService projectLocalService;
    private final RabbitMQProducer rabbitMQProducer;

    /**
     * Exécute la Saga de création de projet.
     * Note: PAS de @Transactional global ici ! Une Saga gère des transactions distribuées.
     */
    public ProjectResponseDTO executeProjectCreationSaga(SagaProjectCreationRequestDTO request, Long managerId, Long companyId) {
        log.info("🟢 DÉMARRAGE SAGA : Création du projet '{}'", request.getName());

        // 🔹 ÉTAPE 1 : Transaction Locale (Création en statut PENDING)
        Project pendingProject = projectLocalService.createProjectWithStatus(request, managerId, companyId, ProjectStatus.PENDING);

        try {
            // 🔹 ÉTAPE 2 : Appel Externe Synchrone (Vérification et assignation via gitdock-auth)
            log.info("SAGA - Étape 2 : Assignation de l'équipe {}", request.getTeamIds());
            projectLocalService.assignTeamToProject(pendingProject.getId(), request.getTeamIds(), managerId);

            // 🔹 ÉTAPE 3 : Appel Externe Asynchrone (RabbitMQ pour gitdock-sync/aspirateur)
            log.info("SAGA - Étape 3 : Demande d'initialisation du Repo Git");
            rabbitMQProducer.sendRepoInitRequest(new RepoInitEvent(pendingProject.getId(), request.getRepoUrl()));

            // 🔹 ÉTAPE 4 : Validation (Commit de la Saga)
            log.info("SAGA - Étape 4 : Succès. Passage au statut ACTIVE");
            Project activeProject = projectLocalService.updateProjectStatus(pendingProject.getId(), ProjectStatus.ACTIVE);

            // Notification de succès
            rabbitMQProducer.sendNotification(managerId, "Projet IA Créé", "Le projet " + activeProject.getName() + " et son équipe sont prêts !");

            log.info("✅ FIN SAGA : Projet créé avec succès.");
            return ProjectMapper.toResponse(activeProject);

        } catch (Exception e) {
            // 🚨 COMPENSATION (LE ROLLBACK DE LA SAGA)
            log.error("❌ ÉCHEC SAGA : Erreur rencontrée ({}). Déclenchement de la compensation...", e.getMessage());
            executeCompensation(pendingProject, managerId, e.getMessage());

            throw new SagaExecutionException("Échec de la création du projet : " + e.getMessage());
        }
    }

    /**
     * Les actions d'annulation si une étape échoue.
     */
    private void executeCompensation(Project pendingProject, Long managerId, String reason) {
        try {
            // 1. Hard Delete du projet PENDING (ou passage en statut FAILED)
            projectLocalService.hardDeleteProject(pendingProject.getId());
            log.info("⏪ ROLLBACK : Projet {} supprimé de la base locale.", pendingProject.getId());

            // 2. Avertir le manager
            rabbitMQProducer.sendNotification(managerId, "Échec Saga", "Échec de la constitution de l'équipe : " + reason);

        } catch (Exception rollbackEx) {
            // Le pire scénario : le rollback échoue. En entreprise, on logge ça avec une alerte CRITIQUE
            // pour qu'un administrateur intervienne manuellement.
            log.error("💥 ALERTE CRITIQUE : Échec du Rollback pour le projet {}. Intervention manuelle requise. Raison: {}",
                    pendingProject.getId(), rollbackEx.getMessage());
        }
    }
}