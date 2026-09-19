<?php

namespace App\Message\Producer;

use PhpAmqpLib\Connection\AMQPStreamConnection;
use PhpAmqpLib\Message\AMQPMessage;
use App\Message\TaskCompletedEvent;
use App\Message\AmqpConnectionFactory;

class TaskEventProducer implements TaskEventProducerInterface
{
    private AMQPStreamConnection $connection;

    public function __construct(AmqpConnectionFactory $connections)
    {
        $this->connection = $connections->create();
    }

    public function publishTaskCompleted(TaskCompletedEvent $event): void
    {
        $channel = $this->connection->channel();

        $channel->exchange_declare(
            'task.exchange',
            'topic',
            false,
            true,
            false
        );

        $payload = json_encode([
            'taskId' => $event->taskId,
            'userId' => $event->userId,
            'taskLevel' => $event->levelId,
            'xpReward' => $event->xpReward,
        ], JSON_THROW_ON_ERROR);

        $message = new AMQPMessage(
            $payload,
            [
                'content_type' => 'application/json',
                'delivery_mode' => 2
            ]
        );

        $channel->basic_publish(
            $message,
            'task.exchange',
            'task.completed'
        );

        $channel->close();
    }

    public function __destruct()
    {
        $this->connection->close();
    }
}
