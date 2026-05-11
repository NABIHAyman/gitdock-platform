<?php

namespace App\Client;

use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Component\HttpFoundation\RequestStack;

class AuthClient
{
    public function __construct(
        private HttpClientInterface $client,
        private RequestStack        $requestStack,
        private string              $authBaseUrl,
        private string              $projectBaseUrl,
        private string              $jwtSecret
    ) {}

    private function getBearerToken(): string
    {
        $request = $this->requestStack->getCurrentRequest();
        if (!$request) return '';
        return $request->headers->get('Authorization') ?? '';
    }

    // ✅ Validation JWT locale — pas d'appel HTTP !
    public function validateToken(string $token): ?array
    {
        try {
            $decoded = JWT::decode(
                $token,
                new Key($this->jwtSecret, 'HS512')
            );

            return [
                'id'        => $decoded->userId ?? null,
                'email'     => $decoded->sub ?? '',
                'companyId' => $decoded->companyId ?? null,
                'roles'     => $decoded->authorities ?? [],
            ];
        } catch (\Throwable $e) {
            return null;
        }
    }

    // ✅ Collaborateurs d'un projet
    public function getProjectCollaborators(int $projectId, string $token): array
    {
        try {
            $response = $this->client->request(
                'GET',
                $this->projectBaseUrl . "/api/projects/{$projectId}/collaborators",
                [
                    'headers' => [
                        'Authorization' => 'Bearer ' . $token,
                        'Accept'        => 'application/json',
                    ],
                    'timeout' => 5,
                ]
            );

            if ($response->getStatusCode() !== 200) return [];

            $data = $response->toArray();
            $list = $data['collaborators'] ?? $data['data'] ?? $data;
            if (!is_array($list)) return [];

            return array_map(fn($u) => [
                'id'       => $u['id'] ?? null,
                'fullName' => $u['fullName'] ?? trim(
                    ($u['firstName'] ?? '') . ' ' . ($u['lastName'] ?? '')
                ),
            ], $list);

        } catch (\Throwable $e) {
            return [];
        }
    }
}
