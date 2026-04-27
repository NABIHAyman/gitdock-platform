<?php

namespace App\MessageProducer;

use App\DTOs\TaskCompletedEventDTO;
use Symfony\Component\Messenger\MessageBusInterface;
use Symfony\Component\Messenger\Bridge\Amqp\Transport\AmqpStamp;

class TaskEventPublisher
{
    public function __construct(private MessageBusInterface $bus)
    {
    }

    public function publishTaskCompleted(TaskCompletedEventDTO $dto): void
    {
        // Envoi explicite sur la clé de routage demandée
        $this->bus->dispatch($dto, [new AmqpStamp('task.completed')]);
    }
}
