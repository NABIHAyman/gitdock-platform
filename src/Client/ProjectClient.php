<?php
namespace App\Client;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class ProjectClient implements ProjectClientInterface
{
    public function __construct(
        private HttpClientInterface $client,
        private string $baseUrl
    ) {}

    public function getProject(int $id): ?array
    {
        try {
            $response = $this->client->request(
                'GET',
                $this->baseUrl . "/api/projects/$id",
                [
                    'headers' => [
                        'Authorization' => $_SERVER['HTTP_AUTHORIZATION'] ?? ''
                    ],
                    'timeout' => 5
                ]
            );

            if ($response->getStatusCode() !== 200) {
                return null;
            }

            return $response->toArray();

        } catch (\Throwable $e) {
            return null;
        }
    }

    public function projectExists(int $id): bool
    {
        return $this->getProject($id) !== null;
    }
}
