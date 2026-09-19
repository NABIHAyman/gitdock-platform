<?php
namespace App\Client;

use Psr\Log\LoggerInterface;

class AuthClientDecorator implements AuthClientInterface
{
    public function __construct(
        private AuthClientInterface $client,
        private LoggerInterface $logger
    ) {}

    public function getUserById(int $id): ?array
    {
        try {
            $this->logger->info("AuthClient getUserById", ['id' => $id]);

            return $this->client->getUserById($id);

        } catch (\Throwable $e) {
            $this->logger->error("AuthClient error", [
                'message' => $e->getMessage()
            ]);

            return null;
        }
    }

    public function getUsersByIds(array $ids): array
    {
        try {
            $this->logger->info("AuthClient getUsersByIds", ['ids' => $ids]);

            return $this->client->getUsersByIds($ids);

        } catch (\Throwable $e) {
            $this->logger->error("AuthClient error", [
                'message' => $e->getMessage()
            ]);

            return [];
        }
    }
}
