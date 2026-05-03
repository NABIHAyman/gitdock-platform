<?php

namespace App\MessageHandler;

use App\DTOs\CommitSavedEventDTO;
use App\DTOs\TaskCompletedEventDTO;
use App\MessageProducer\TaskEventPublisher;
use App\Repository\TaskRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Messenger\Attribute\AsMessageHandler;

#[AsMessageHandler]
class CommitSavedHandler
{
    public function __construct(
        private TaskRepository $taskRepo,
        private EntityManagerInterface $em,
        private TaskEventPublisher $taskEventPublisher
    ) {}

    public function __invoke(CommitSavedEventDTO $event): void
    {
        // Parsing "Fixes #ID" (Smart Close)
        if (preg_match('/Fixes\s+#(\d+)/i', $event->message, $matches)) {
            $taskId = (int) $matches[1];
            $task = $this->taskRepo->find($taskId);

            if ($task && $task->getProjectId() === $event->projectId) {
                if ($task->getStatus() !== 'Done') {
                    $task->setStatus('Done');
                    $task->setUpdatedAt(new \DateTimeImmutable());
                    
                    $this->em->flush();

                    // Rétro-notifier la Gamification via le MessageProducer créé à l'étape 4
                    $xpReward = $task->getTaskLevel() ? $task->getTaskLevel()->getXpReward() : 0;
                    $this->taskEventPublisher->publishTaskCompleted(
                        new TaskCompletedEventDTO(
                            $task->getId(),
                            $task->getProjectId(),
                            $task->getAssignedToUserId(),
                            $xpReward
                        )
                    );
                }
            }
        }
    }
}
