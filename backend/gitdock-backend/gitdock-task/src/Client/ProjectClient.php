<?php
namespace App\Client;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Component\HttpFoundation\RequestStack;

class ProjectClient implements ProjectClientInterface
{
    public function __construct(
        private HttpClientInterface $client,
        private string $baseUrl,
        private RequestStack $requestStack
    ) {}

    private function getBearerToken(): string
    {
        $request = $this->requestStack->getCurrentRequest();
        return $request?->headers->get('Authorization') ?? '';
    }

    public function getProject(int $id): ?array
    {
        try {
            $response = $this->client->request('GET',
                $this->baseUrl . "/api/projects/$id",
                [
                    'headers' => [
                        'Authorization' => $this->getBearerToken(),
                        'Accept' => 'application/json',
                    ],
                    'timeout' => 5
                ]
            );
            return $response->getStatusCode() === 200 ? $response->toArray() : null;
        } catch (\Throwable $e) {
            return null;
        }
    }

    public function projectExists(int $id): bool
    {
        return $this->getProject($id) !== null;
    }

    // ← NOUVEAU : liste les projets du manager connecté
    public function getManagerProjects(): array
    {
        try {
            $response = $this->client->request('GET',
                $this->baseUrl . "/api/projects",
                [
                    'headers' => [
                        'Authorization' => $this->getBearerToken(),
                        'Accept' => 'application/json',
                    ],
                    'timeout' => 5
                ]
            );
            $status = $response->getStatusCode();
            error_log('ProjectClient status: ' . $status);
            if ($status !== 200) return [];
            $data = $response->toArray();
            return is_array($data) ? $data : [];
        } catch (\Throwable $e) {
            error_log('ProjectClient error: ' . $e->getMessage());
            return [];
        }
    }
}
