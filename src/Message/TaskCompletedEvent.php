<?php

declare(strict_types=1);

namespace App\Message;

final class TaskCompletedEvent
{
    public function __construct(
        public readonly int $taskId,
        public readonly int $userId,
        public readonly ?int $levelId,
        public readonly int $xpReward
    ) {}
}
