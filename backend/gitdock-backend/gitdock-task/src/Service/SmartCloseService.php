<?php

namespace App\Service;

use App\Entity\Task;
use App\Enum\TaskStatus;
use App\Repository\TaskRepository;

/**
 * Smart Close : un commit dont le message contient « Fixes #ID » clôture la
 * tâche ID, si elle appartient au même projet. La clôture passe par
 * TaskService::markAsDone(), qui publie TaskCompletedEvent pour la gamification.
 */
class SmartCloseService
{
    public const PATTERN = '/Fixes\s+#(\d+)/i';

    public function __construct(
        private TaskRepository $repo,
        private TaskService $taskService
    ) {}

    public static function extractTaskId(string $commitMessage): ?int
    {
        return preg_match(self::PATTERN, $commitMessage, $matches) ? (int) $matches[1] : null;
    }

    /**
     * Retourne la tâche clôturée, ou null si le commit ne clôture rien.
     */
    public function handleCommit(string $commitMessage, int $projectId): ?Task
    {
        $taskId = self::extractTaskId($commitMessage);
        if ($taskId === null) {
            return null;
        }

        $task = $this->repo->find($taskId);
        if (
            !$task
            || $task->isDeleted()
            || $task->getProjectId() !== $projectId
            || !$task->getAssignedTo()
            || in_array($task->getStatus(), [TaskStatus::DONE, TaskStatus::CANCELED], true)
        ) {
            return null;
        }

        return $this->taskService->markAsDone($taskId);
    }
}
