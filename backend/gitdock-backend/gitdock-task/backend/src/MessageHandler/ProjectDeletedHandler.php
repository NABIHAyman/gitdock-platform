<?php

namespace App\MessageHandler;

use App\DTOs\ProjectDeletedEventDTO;
use App\Repository\TaskRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Messenger\Attribute\AsMessageHandler;

#[AsMessageHandler]
class ProjectDeletedHandler
{
    public function __construct(
        private TaskRepository $taskRepo,
        private EntityManagerInterface $em
    ) {}

    public function __invoke(ProjectDeletedEventDTO $event): void
    {
        // Cascade Soft Delete des tâches du projet
        $tasks = $this->taskRepo->findBy(['projectId' => $event->projectId]);
        
        $now = new \DateTime();
        foreach ($tasks as $task) {
            $task->setDeletedAt($now);
        }

        $this->em->flush();
    }
}
