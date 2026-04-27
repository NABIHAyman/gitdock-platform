<?php

namespace App\DTOs\DTOtasks;

class TaskDto
{
public ?string $title = null;
    public ?string $description = null;
    public ?string $status = null;
    public ?string $dueDate = null;

    public ?int $assignedTo = null;
    public ?int $assignedBy = null;

    public ?int $epic = null;
    public ?int $part = null;
    public ?int $level = null;
}
