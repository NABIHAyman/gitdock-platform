<?php
namespace App\Client;

use Psr\Log\LoggerInterface;

class ProjectClientDecorator implements ProjectClientInterface
{
    public function __construct(
        private ProjectClientInterface $client,
        private LoggerInterface $logger
    ) {}

    public function getProject(int $id): ?array
    {
        $this->logger->info("➡ Calling Project service", [
            'projectId' => $id
        ]);

        $result = $this->client->getProject($id);

        if ($result === null) {
            $this->logger->warning("❌ Project not found", [
                'projectId' => $id
            ]);
        }

        return $result;
    }

    public function projectExists(int $id): bool
    {
        return $this->getProject($id) !== null;
    }
}
