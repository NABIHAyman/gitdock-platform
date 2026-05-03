<?php

namespace App\DTOs;

class UserDeletedEventDTO
{
    public function __construct(
        public readonly int $userId
    ) {}
}
