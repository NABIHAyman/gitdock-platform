<?php

namespace App\DTOs\DTOtasks;

class TaskDto
{
    public ?string $title = null;
    public ?string $description = null;

    // ENUM (string venant du front)
    public ?string $status = null;
    public ?string $priority = null;

    public ?string $dueDate = null;

    public ?string $assignedTo = null;
    public ?string $assignedBy = null;

    public ?int $epicId = null;
    public ?int $levelId = null;
    public ?int $projectId = null;

    public ?int $partId = null;
}
