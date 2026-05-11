<?php

namespace App\DTOs;

class ProjectDeletedEventDTO
{
    public function __construct(
        public readonly int $projectId
    ) {}
}
