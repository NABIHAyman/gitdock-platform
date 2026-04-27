<?php

namespace App\Client;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Psr\Log\LoggerInterface;

class ProjectServiceClient
{
    private string $baseUrl;

    public function __construct(
        private HttpClientInterface $httpClient,
        private LoggerInterface $logger
    ) {
        // Fallback sur le nom DNS de service interne
        $this->baseUrl = $_ENV['PROJECT_SERVICE_URL'] ?? 'http://gitdock-project';
    }

    public function projectExists(int $projectId): bool
    {
        try {
            $response = $this->httpClient->request('GET', "{$this->baseUrl}/api/projects/{$projectId}");
            
            return $response->getStatusCode() === 200;
        } catch (\Throwable $e) {
            $this->logger->error('Erreur de communication avec gitdock-project (projectExists) : ' . $e->getMessage());
            return false;
        }
    }
}
