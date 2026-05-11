<?php

namespace App\Message;

class UserRegisteredMessage
{
    public function __construct(
        private int $id,
        private string $fullName,
        private string $email
    ) {}

    public function getId(): int { return $this->id; }
    public function getFullName(): string { return $this->fullName; }
    public function getEmail(): string { return $this->email; }
}
