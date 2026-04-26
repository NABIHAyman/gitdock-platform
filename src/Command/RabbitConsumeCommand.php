<?php

namespace App\Command;

use App\Service\UserSyncService;
use PhpAmqpLib\Connection\AMQPStreamConnection;
use PhpAmqpLib\Exchange\AMQPExchangeType;
use PhpAmqpLib\Message\AMQPMessage;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

#[AsCommand(name: 'app:rabbit:consume')]
class RabbitConsumeCommand extends Command
{
    public function __construct(
        private UserSyncService $userSyncService
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $connection = new AMQPStreamConnection(
            'rabbitmq', // ⚠️ si docker
            5672,
            'guest',
            'guest'
        );

        $channel = $connection->channel();

        // Exchange (IMPORTANT: match Spring)
        $channel->exchange_declare(
            'auth.exchange',
            AMQPExchangeType::TOPIC,
            false,
            true,
            false
        );

        // Queue durable
        $channel->queue_declare('auth.queue', false, true, false, false);

        // Bindings
        $channel->queue_bind('auth.queue', 'auth.exchange', 'user.*');

        $output->writeln("🚀 Waiting for events...");

        $callback = function (AMQPMessage $msg) use ($output) {

            $body = json_decode($msg->getBody(), true);

            if (!$body) {
                $output->writeln("❌ Invalid JSON message");
                return;
            }

            $type = $body['type'] ?? 'UNKNOWN';
            $payload = $body['data'] ?? [];

            $output->writeln("📩 EVENT: " . $type);
            $output->writeln(print_r($payload, true));

            // 🔥 SYNC vers DB Symfony
            $this->userSyncService->sync($type, $payload);
        };

        $channel->basic_consume(
            'auth.queue',
            '',
            false,
            true,
            false,
            false,
            $callback
        );

        while ($channel->is_consuming()) {
            $channel->wait();
        }

        return Command::SUCCESS;
    }
}