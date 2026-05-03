<?php

namespace App\MessageHandler;

use App\DTOs\UserDeletedEventDTO;
use App\Repository\TaskRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Messenger\Attribute\AsMessageHandler;

#[AsMessageHandler]
class UserDeletedHandler
{
    public function __construct(
        private TaskRepository $taskRepo,
        private EntityManagerInterface $em
    ) {}

    public function __invoke(UserDeletedEventDTO $event): void
    {
        // Unassign l'utilisateur fantôme
        $tasks = $this->taskRepo->findBy(['assignedToUserId' => $event->userId]);
        
        foreach ($tasks as $task) {
            $task->setAssignedToUserId(null); // Ghost user effect
            $task->setUpdatedAt(new \DateTimeImmutable());
        }

        $this->em->flush();
    }
}
