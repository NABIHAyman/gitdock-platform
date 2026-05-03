<?php

namespace App\MessageProducer;

use App\DTOs\NotificationEventDTO;
use Symfony\Component\Messenger\MessageBusInterface;
use Symfony\Component\Messenger\Bridge\Amqp\Transport\AmqpStamp;

class NotificationSender
{
    public function __construct(private MessageBusInterface $bus)
    {
    }

    public function send(NotificationEventDTO $dto): void
    {
        // Envoi explicite sur la clé de routage demandée
        $this->bus->dispatch($dto, [new AmqpStamp('notification.routing.key')]);
    }
}
