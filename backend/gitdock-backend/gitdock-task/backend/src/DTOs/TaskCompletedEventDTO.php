<?php

namespace App\DTOs;

class TaskCompletedEventDTO
{
    public function __construct(
        public readonly int $taskId,
        public readonly ?int $projectId,
        public readonly ?int $userId,
        public readonly ?int $xpReward
    ) {}
}
