<?php

namespace App\Message\Producer;

use App\Message\TaskCompletedEvent;

interface TaskEventProducerInterface
{
    public function publishTaskCompleted(TaskCompletedEvent $event): void;
}
