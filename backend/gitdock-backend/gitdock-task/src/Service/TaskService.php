<?php

namespace App\Service;

use App\Entity\Task;
use App\DTOs\DTOtasks\TaskDto;
use App\Repository\TaskRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;

class TaskService
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private TaskRepository $taskRepository,
        private UserRepository $userRepository,
    ) {}

    public function getAll(): array
    {
        return $this->taskRepository->findNotDeleted();
    }

    public function getById(int $id): Task
    {
        $task = $this->taskRepository->find($id);
        if (!$task || $task->isDeleted()) {
            throw new \Exception("Task not found");
        }
        return $task;
    }

    public function create(TaskDto $dto): Task
    {
        $task = new Task();
        $this->hydrate($task, $dto);
        $this->entityManager->persist($task);
        $this->entityManager->flush();
        return $task;
    }

    public function update(int $id, TaskDto $dto): Task
    {
        $task = $this->taskRepository->find($id);
        if (!$task || $task->isDeleted()) {
            throw new \Exception("Task not found");
        }
        $this->hydrate($task, $dto);
        $this->entityManager->flush();
        return $task;
    }

    public function softDelete(int $id): void
    {
        $task = $this->getById($id);
        $task->setDeletedAt(new \DateTime());
        $this->entityManager->flush();
    }

    public function markAsDone(int $id): Task
    {
        $task = $this->getById($id);
        $task->setStatus(\App\Enum\TaskStatus::DONE);
        if (method_exists($task, 'setCompletedAt')) {
            $task->setCompletedAt(new \DateTimeImmutable());
        }
        $this->entityManager->flush();
        return $task;
    }

    private function hydrate(Task $task, TaskDto $dto): void
    {
        if ($dto->title) {
            $task->setTitle($dto->title);
        }

        if ($dto->description !== null) {
            $task->setDescription($dto->description);
        }

        if ($dto->status) {
            // ✅ CORRIGÉ : ne pas supprimer les underscores
            // 'in_progress' doit rester 'in_progress' pour matcher l'enum
            $status = \App\Enum\TaskStatus::tryFrom(strtolower($dto->status))
                   ?? \App\Enum\TaskStatus::TODO;
            $task->setStatus($status);
        }

        if ($dto->priority) {
            $priority = \App\Enum\TaskPriority::tryFrom(strtolower($dto->priority))
                     ?? \App\Enum\TaskPriority::MEDIUM;
            $task->setPriority($priority);
        }

        if ($dto->dueDate) {
            try {
                $task->setDueDate(new \DateTime($dto->dueDate));
            } catch (\Exception $e) {}
        }

        if ($dto->assignedTo !== null) {
            $user = $this->userRepository->find((int) $dto->assignedTo);
            $task->setAssignedTo($user);
        }

        if ($dto->projectId !== null) {
            $task->setProjectId((int) $dto->projectId);
        }
    }
}
