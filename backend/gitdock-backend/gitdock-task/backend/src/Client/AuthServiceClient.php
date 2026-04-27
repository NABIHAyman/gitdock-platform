<?php

namespace App\Client;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Psr\Log\LoggerInterface;

class AuthServiceClient
{
    private string $baseUrl;

    public function __construct(
        private HttpClientInterface $httpClient,
        private LoggerInterface $logger
    ) {
        // Utilisation de host.docker.internal pour atteindre IntelliJ depuis Docker !
        // Change le port 8080 par le port de ta Gateway (ou de l'Auth) si différent.
        $this->baseUrl = $_ENV['AUTH_SERVICE_URL'] ?? 'http://host.docker.internal:8080';
    }

    public function userExists(int $userId): bool
    {
        // ... (Tu pourras aussi ajouter le token ici plus tard si besoin)
    }

    // ON AJOUTE LE PARAMÈTRE $token
    public function getAllUsersSummary(?string $token): array
    {
        try {
            // On prépare les options avec le header d'authentification
            $options = [];
            if ($token) {
                $options['headers'] = [
                    'Authorization' => $token
                ];
            }

            $response = $this->httpClient->request('GET', "{$this->baseUrl}/api/auth/users/summaries", $options);

            if ($response->getStatusCode() === 200) {
                return $response->toArray();
            } else {
                $this->logger->error('gitdock-auth a répondu avec le code : ' . $response->getStatusCode());
            }
        } catch (\Throwable $e) {
            $this->logger->error('Erreur de communication avec gitdock-auth : ' . $e->getMessage());
        }

        return [];
    }
}
