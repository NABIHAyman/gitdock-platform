<?php

namespace App\Command;

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
    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $connection = new AMQPStreamConnection(
            'rabbitmq',
            5672,
            'guest',
            'guest'
        );

        $channel = $connection->channel();

        // =========================
        // TASK EXCHANGE
        // =========================
        $channel->exchange_declare(
            'task.exchange',
            AMQPExchangeType::TOPIC,
            false,
            true,
            false
        );

        // =========================
        // QUEUE GAMIFICATION
        // =========================
        $channel->queue_declare(
            'task.completed.queue',
            false,
            true,
            false,
            false
        );

        // =========================
        // BINDING
        // =========================
        $channel->queue_bind(
            'task.completed.queue',
            'task.exchange',
            'task.completed'
        );

        $output->writeln("🚀 Waiting TaskCompletedEvent...");

        // =========================
        // CALLBACK
        // =========================
        $callback = function (AMQPMessage $msg) use ($output) {

            $body = json_decode($msg->getBody(), true);

            if (!$body) {
                $output->writeln("❌ Invalid message");
                return;
            }

            $output->writeln("🎯 TaskCompletedEvent reçu");

            $taskId = $body['taskId'] ?? null;
            $userId = $body['userId'] ?? null;
            $level = $body['taskLevel'] ?? null;
            $xp = $body['xpReward'] ?? 0;

            $output->writeln("Task: $taskId");
            $output->writeln("User: $userId");
            $output->writeln("Level: $level");
            $output->writeln("XP: $xp");

            // TODO: appeler service gamification ici
        };

        $channel->basic_consume(
            'task.completed.queue',
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
