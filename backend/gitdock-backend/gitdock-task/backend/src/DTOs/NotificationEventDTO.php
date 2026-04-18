<?php

namespace App\DTOs;

class NotificationEventDTO
{
    public function __construct(
        public readonly ?int $userId,
        public readonly string $type,
        public readonly string $message
    ) {}
}
