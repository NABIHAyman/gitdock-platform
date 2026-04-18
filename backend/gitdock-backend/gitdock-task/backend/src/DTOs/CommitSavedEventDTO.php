<?php

namespace App\DTOs;

class CommitSavedEventDTO
{
    public function __construct(
        public readonly string $commitSha,
        public readonly string $message,
        public readonly int $projectId,
        public readonly int $authorUserId
    ) {}
}
