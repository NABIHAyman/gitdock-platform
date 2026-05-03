<?php

namespace App\Client;

interface ProjectClientInterface
{
    public function getProject(int $id): ?array;

    public function projectExists(int $id): bool;
}
