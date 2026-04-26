<?php

namespace App\Service;

use App\DTOs\DTOtasks\TaskDto;
use App\Entity\Task;
use App\Factory\TaskFactory;
use App\Repository\TaskRepository;
use Doctrine\ORM\EntityManagerInterface;

class TaskService
{
    public function __construct(
        private EntityManagerInterface $em,
        private TaskRepository $taskRepo,
        private TaskFactory $factory
    ) {}

    // ================= CREATE =================
    public function create(TaskDto $dto): Task
    {
        $task = $this->factory->create($dto);

        $this->em->persist($task);
        $this->em->flush();

        return $task;
    }

    // ================= UPDATE =================
    public function update(int $id, TaskDto $dto): Task
    {
        $task = $this->getById($id);

        if ($dto->title) $task->setTitle($dto->title);
        if ($dto->description) $task->setDescription($dto->description);
        if ($dto->status) $task->setStatus($dto->status);
        if ($dto->dueDate) $task->setDueDate(new \DateTime($dto->dueDate));

        $this->em->flush();

        return $task;
    }

    // ================= GET ALL =================
    public function getAll(): array
    {
        return $this->taskRepo->findBy(['deletedAt' => null]);
    }

    // ================= GET BY ID =================
    public function getById(int $id): Task
    {
        $task = $this->taskRepo->find($id);

        if (!$task || $task->isDeleted()) {
            throw new \Exception("Task not found");
        }

        return $task;
    }

    // ================= SOFT DELETE =================
    public function softDelete(int $id): void
    {
        $task = $this->getById($id);

        $task->setDeletedAt(new \DateTime());

        $this->em->flush();
    }

    // ================= DASHBOARD =================
    public function getDashboardStats(): array
    {
        return [
            'total' => $this->taskRepo->count([]),
            'in_progress' => $this->taskRepo->count(['status' => 'In Progress']),
            'completed' => $this->taskRepo->count(['status' => 'Done']),
        ];
    }
}
