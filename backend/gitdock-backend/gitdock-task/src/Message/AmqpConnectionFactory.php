<?php

namespace App\Message;

use PhpAmqpLib\Connection\AMQPStreamConnection;
use Symfony\Component\DependencyInjection\Attribute\Autowire;

/**
 * Ouvre une connexion RabbitMQ à partir de MESSENGER_TRANSPORT_DSN
 * (ex : amqp://guest:guest@gitdock-rabbitmq:5672/%2f), pour que l'hôte
 * change entre le poste local et Docker sans toucher au code.
 */
final class AmqpConnectionFactory
{
    public function __construct(
        #[Autowire(env: 'MESSENGER_TRANSPORT_DSN')]
        private string $dsn
    ) {}

    public function create(): AMQPStreamConnection
    {
        $parts = parse_url($this->dsn) ?: [];

        $path = $parts['path'] ?? '';
        $vhost = ($path === '' || $path === '/') ? '/' : urldecode(substr($path, 1));

        return new AMQPStreamConnection(
            $parts['host'] ?? '127.0.0.1',
            $parts['port'] ?? 5672,
            urldecode($parts['user'] ?? 'guest'),
            urldecode($parts['pass'] ?? 'guest'),
            $vhost
        );
    }
}
