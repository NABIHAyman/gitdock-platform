<?php

namespace App\Service;

use App\DTOs\DTOtasks\TaskDto;
use Symfony\Component\HttpFoundation\Request;

class TaskMapper
{
    /**
     * Transforme une requête JSON en un objet TaskDto propre.
     */
    public function mapTaskRequestToDto(Request $request): TaskDto
    {
        $data = json_decode($request->getContent(), true);

        if (!is_array($data)) {
            throw new \Exception("Le corps de la requête JSON est invalide ou vide.");
        }

        $dto = new TaskDto();

        // ================= DONNÉES DE BASE =================
        $dto->title = $data['title'] ?? null;
        $dto->description = $data['description'] ?? null;

        // ================= GESTION DES ENUMS =================
        // On s'assure que le statut est en minuscules et sans espaces (ex: "In Progress" -> "in_progress")
        if (!empty($data['status'])) {
            $dto->status = strtolower(str_replace(' ', '_', $data['status']));
        }

        // Pareil pour la priorité (ex: "High" -> "high")
        if (!empty($data['priority'])) {
            $dto->priority = strtolower($data['priority']);
        }

        // ================= DATES =================
        $dto->dueDate = $data['dueDate'] ?? null;

        // ================= RELATIONS (CLEF DU PROBLÈME) =================
        /** * Ici, on accepte 'assignedTo' (nouveau front) OU 'assigneeId' (ancien front/test).
         * C'est ce qui garantit que l'ID de Salma ne sera plus null.
         */
        $dto->assignedTo = $data['assignedTo'] ?? $data['assigneeId'] ?? null;

        $dto->assignedBy = $data['assignedBy'] ?? null;

        // On gère les différentes appellations possibles pour les IDs techniques
        $dto->epicId = $data['epicId'] ?? $data['epic'] ?? null;
        $dto->levelId = $data['levelId'] ?? $data['level'] ?? null;
        $dto->partId = $data['partId'] ?? $data['part'] ?? null;
        $dto->projectId = $data['projectId'] ?? null;

        return $dto;
    }
}
