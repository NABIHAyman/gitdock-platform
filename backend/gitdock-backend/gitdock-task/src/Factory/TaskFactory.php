<?php

namespace App\Factory;

use App\Entity\Task;
use App\DTOs\DTOtasks\TaskDto;
use App\Repository\UserRepository;
use App\Repository\EpicRepository;
use App\Repository\LevelRepository;

class TaskFactory
{
    public function __construct(
        private UserRepository $userRepo,
        private EpicRepository $epicRepo,
        private LevelRepository $levelRepo
    ) {}

    public function create(TaskDto $dto): Task
    {
        $task = new Task();

        // ================= BASIC FIELDS =================
        $task->setTitle($dto->title ?? 'Untitled');
        $task->setDescription($dto->description ?? '');
        $task->setStatus($dto->status ?? 'To Do');
        $task->setDueDate(
            new \DateTime($dto->dueDate ?? 'now')
        );

        $task->setCreatedAt(new \DateTimeImmutable());
        $task->setUpdatedAt(new \DateTimeImmutable());

        // ================= RELATIONS =================

        // assignedTo
        if ($dto->assignedTo) {
            $task->setAssignedTo(
                $this->userRepo->find($dto->assignedTo)
            );
        }

        // assignedBy (si tu veux gérer ici aussi)
        if ($dto->assignedBy) {
            $task->setAssignedBy(
                $this->userRepo->find($dto->assignedBy)
            );
        }

        // epic
        if ($dto->epic) {
            $task->setEpic(
                $this->epicRepo->find($dto->epic)
            );
        }

        // level
        if ($dto->level) {
            $task->setLevel(
                $this->levelRepo->find($dto->level)
            );
        }

        return $task;
    }
}