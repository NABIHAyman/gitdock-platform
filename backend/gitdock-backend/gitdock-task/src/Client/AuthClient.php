<?php

namespace App\Client;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface;
use Symfony\Contracts\HttpClient\Exception\ClientExceptionInterface;
use Symfony\Contracts\HttpClient\Exception\ServerExceptionInterface;

class AuthClient implements AuthClientInterface
{
    public function __construct(
        private HttpClientInterface $client,
        private string $authBaseUrl
    ) {}

    public function getUserById(int $id): ?array
    {
        try {
            $response = $this->client->request(
                'GET',
                $this->authBaseUrl . "/users/$id"
            );

            if ($response->getStatusCode() !== 200) {
                return null;
            }

            return $response->toArray();

        } catch (\Throwable $e) {
            return null;
        }
    }

    public function getUsersByIds(array $ids): array
    {
        if (empty($ids)) {
            return [];
        }

        try {
            $response = $this->client->request(
                'GET',
                $this->authBaseUrl . '/users',
                [
                    'query' => ['ids' => implode(',', $ids)]
                ]
            );

            if ($response->getStatusCode() !== 200) {
                return [];
            }

            return $response->toArray();

        } catch (\Throwable $e) {
            return [];
        }
    }
}
