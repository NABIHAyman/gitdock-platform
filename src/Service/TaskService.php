<?php

namespace App\Service;

use App\Entity\Task;
use App\DTOs\DTOtasks\TaskDto;
use Doctrine\ORM\EntityManagerInterface;
use App\Repository\UserRepository;
use App\Repository\EpicRepository;
use App\Repository\PartRepository;
use App\Repository\LevelRepository;
use App\Repository\TaskRepository;

class TaskService
{
    public function __construct(
        private EntityManagerInterface $em,
        private UserRepository $userRepo,
        private EpicRepository $epicRepo,
        private PartRepository $partRepo,
        private LevelRepository $levelRepo,
        private TaskRepository $taskRepo
    ) {}

    // ---------------- CREATE ----------------
    public function create(TaskDto $dto): Task
    {
        $task = new Task();

        $task->setTitle($dto->title ?? 'Untitled Task');
        $task->setDescription($dto->description ?? '');
        $task->setStatus($dto->status ?? 'To Do');
        $task->setDueDate($dto->dueDate ? new \DateTime($dto->dueDate) : new \DateTime());
        $task->setCreatedAt(new \DateTimeImmutable());
        $task->setUpdatedAt(new \DateTimeImmutable());

        // Relations
        if ($dto->assignedTo) $task->setAssignedTo($this->userRepo->find($dto->assignedTo));
        if ($dto->assignedBy) $task->setAssignedBy($this->userRepo->find($dto->assignedBy));
        if ($dto->epic) $task->setEpic($this->epicRepo->find($dto->epic));
        if ($dto->part) $task->setPart($this->partRepo->find($dto->part));
        if ($dto->level) $task->setLevel($this->levelRepo->find($dto->level));

        $this->em->persist($task);
        $this->em->flush();

        return $task;
    }

    // ---------------- UPDATE ----------------
    public function update(int $id, TaskDto $dto): Task
    {
        $task = $this->em->getRepository(Task::class)->find($id);
        if (!$task) {
            throw new \Exception("Task introuvable (ID=$id)");
        }

        if ($dto->title !== null) $task->setTitle($dto->title);
        if ($dto->description !== null) $task->setDescription($dto->description);
        if ($dto->status !== null) $task->setStatus($dto->status);
        if ($dto->dueDate !== null) $task->setDueDate(new \DateTime($dto->dueDate));

        // Relations
        if ($dto->assignedTo) $task->setAssignedTo($this->userRepo->find($dto->assignedTo));
        if ($dto->assignedBy) $task->setAssignedBy($this->userRepo->find($dto->assignedBy));
        if ($dto->epic) $task->setEpic($this->epicRepo->find($dto->epic));
        if ($dto->part) $task->setPart($this->partRepo->find($dto->part));
        if ($dto->level) $task->setLevel($this->levelRepo->find($dto->level));

        $task->setUpdatedAt(new \DateTimeImmutable());

        $this->em->flush();

        return $task;
    }

    // ---------------- GET ALL ----------------
    public function getAll(): array
    {
        return $this->em->getRepository(Task::class)->findAll();
    }
    public function getById(int $id): Task
{
    $task = $this->em->getRepository(Task::class)->find($id);

    if (!$task) {
        throw new \Exception("Task not found");
    }

    return $task;
}
public function softDelete(int $id)
{
    $task = $this->taskRepo->find($id);

    if (!$task) {
        throw new \Exception("Task not found");
    }

    $task->setDeletedAt(new \DateTime()); // On met la date actuelle
    $this->em->flush();

    return $task;
}
}
