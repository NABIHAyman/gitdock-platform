<?php
namespace App\Tests\Client;

use App\Client\ProjectClient;
use PHPUnit\Framework\TestCase;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Contracts\HttpClient\ResponseInterface;

class ProjectClientTest extends TestCase
{
    public function testGetProjectReturnsData(): void

    {
        // 🔹 Mock response HTTP
        $responseMock = $this->createMock(ResponseInterface::class);
        $responseMock->method('getStatusCode')->willReturn(200);
        $responseMock->method('toArray')->willReturn([
            'id' => 1,
            'name' => 'PFA Project'
        ]);

        // 🔹 Mock HttpClient
        $httpClientMock = $this->createMock(HttpClientInterface::class);
        $httpClientMock->method('request')->willReturn($responseMock);

        // 🔹 Service testé
        $client = new ProjectClient(
            $httpClientMock,
            'http://fake-url'
        );

        $result = $client->getProject(1);

        // 🔥 Assertions
        $this->assertNotNull($result);
        $this->assertEquals(1, $result['id']);
        $this->assertEquals('PFA Project', $result['name']);
    }
}
