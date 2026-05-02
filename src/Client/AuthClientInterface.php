<?php
namespace App\Client;

interface AuthClientInterface
{
    public function getUserById(int $id): ?array;
    public function getUsersByIds(array $ids): array;
}
