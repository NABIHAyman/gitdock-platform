<?php
namespace App\Service;

use App\Message\TaskCompletedEvent;
use Symfony\Component\Messenger\MessageBusInterface;

class TaskEventProducer
{
    public function __construct(
        private MessageBusInterface $bus
    ) {}

    public function send(
        int $taskId,
        int $userId,
        ?int $levelId,
        int $xpReward
    ): void {
        $this->bus->dispatch(
            new TaskCompletedEvent(
                $taskId,
                $userId,
                $levelId,
                $xpReward
            )
        );
    }
}
