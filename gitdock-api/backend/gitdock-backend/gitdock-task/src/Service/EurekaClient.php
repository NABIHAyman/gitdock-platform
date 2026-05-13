<?php
namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Psr\Log\LoggerInterface;

class EurekaClient
{
    private string $ipAddr = '127.0.0.1'; // IP locale de ton serveur Symfony

    public function __construct(
        private HttpClientInterface $httpClient,
        private LoggerInterface $logger,
        private string $eurekaUrl,
        private string $appName,
        private int $appPort
    ) {}

    public function register(): void
    {
        $instanceId = $this->ipAddr . ':' . $this->appName . ':' . $this->appPort;

        $payload = [
            'instance' => [
                'instanceId' => $instanceId,
                'hostName' => $this->ipAddr,
                'app' => $this->appName,
                'ipAddr' => $this->ipAddr,
                'status' => 'UP',
                'port' => ['$' => $this->appPort, '@enabled' => 'true'],
                'vipAddress' => strtolower($this->appName),
                'dataCenterInfo' => [
                    '@class' => 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
                    'name' => 'MyOwn'
                ]
            ]
        ];

        try {
            $response = $this->httpClient->request('POST', $this->eurekaUrl . '/apps/' . $this->appName, [
                'json' => $payload,
                'headers' => ['Content-Type' => 'application/json']
            ]);

            if ($response->getStatusCode() === 204) {
                $this->logger->info("✅ Enregistrement Eureka réussi pour $this->appName");
            }
        } catch (\Exception $e) {
            $this->logger->error("❌ Échec de l'enregistrement Eureka : " . $e->getMessage());
        }
    }

    public function heartbeat(): void
    {
        $instanceId = $this->ipAddr . ':' . $this->appName . ':' . $this->appPort;
        try {
            $response = $this->httpClient->request('PUT', $this->eurekaUrl . '/apps/' . $this->appName . '/' . $instanceId);

            // Si Eureka a oublié l'instance (ex: redémarrage d'Eureka), on se réenregistre
            if ($response->getStatusCode() === 404) {
                $this->logger->warning("⚠️ Eureka ne nous connaît plus. Ré-enregistrement...");
                $this->register();
            }
        } catch (\Exception $e) {
            $this->logger->error("⚠️ Échec du Heartbeat Eureka : " . $e->getMessage());
        }
    }
}
