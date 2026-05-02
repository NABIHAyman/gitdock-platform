<?php

namespace App\Service;

use App\DTOs\DTOtasks\TaskDto;
use Symfony\Component\HttpFoundation\Request;

class TaskMapper
{
    public function mapTaskRequestToDto(Request $request): TaskDto
    {
        $data = json_decode($request->getContent(), true);

        if (!is_array($data)) {
            throw new \Exception("Invalid JSON body");
        }

        $dto = new TaskDto();

        // ================= BASIC =================
        $dto->title = $data['title'] ?? null;
        $dto->description = $data['description'] ?? null;

        // ================= STATUS =================
        if (!empty($data['status'])) {
            $dto->status = strtolower(str_replace(' ', '_', $data['status']));
        }

        // ================= PRIORITY =================
        if (!empty($data['priority'])) {
            $dto->priority = strtolower($data['priority']);
        }

        // ================= DATE =================
        $dto->dueDate = $data['dueDate'] ?? null;

        // ================= RELATIONS =================
        $dto->assignedTo = $data['assignedTo'] ?? null;
        $dto->assignedBy = $data['assignedBy'] ?? null;

        $dto->epicId = $data['epicId'] ?? $data['epic'] ?? null;
        $dto->levelId = $data['levelId'] ?? $data['level'] ?? null;
        $dto->partId = $data['partId'] ?? $data['part'] ?? null;
        $dto->projectId = $data['projectId'] ?? null;

        return $dto;
    }
}
