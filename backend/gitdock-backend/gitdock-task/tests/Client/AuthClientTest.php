<?php

namespace App\Tests\Client;

use App\Client\AuthClient;
use PHPUnit\Framework\TestCase;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Contracts\HttpClient\ResponseInterface;

class AuthClientTest extends TestCase
{
    public function testGetUserByIdReturnsData(): void
    {
        $response = $this->createMock(ResponseInterface::class);

        $response->method('getStatusCode')->willReturn(200);
        $response->method('toArray')->willReturn([
            'id' => 1,
            'firstName' => 'Super',
            'lastName' => 'Admin'
        ]);

        $httpClient = $this->createMock(HttpClientInterface::class);

        $httpClient->method('request')->willReturn($response);

        $client = new AuthClient(
            $httpClient,
            'http://auth-service:8081/api/auth'
        );

        $result = $client->getUserById(1);

        $this->assertNotNull($result);
        $this->assertEquals(1, $result['id']);
        $this->assertEquals('Super', $result['firstName']);
    }

    public function testGetUserByIdReturnsNullOnError(): void
    {
        $response = $this->createMock(ResponseInterface::class);

        // simulation erreur HTTP
        $response->method('getStatusCode')->willReturn(500);

        // important pour éviter retour inattendu []
        $response->method('toArray')->willReturn([]);

        $httpClient = $this->createMock(HttpClientInterface::class);

        $httpClient->method('request')->willReturn($response);

        $client = new AuthClient(
            $httpClient,
            'http://auth-service:8081/api/auth'
        );

        $result = $client->getUserById(1);

        $this->assertNull($result);
    }
}
