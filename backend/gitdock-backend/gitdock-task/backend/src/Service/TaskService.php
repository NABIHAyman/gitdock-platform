<?php

namespace App\Service;

use App\Entity\Task;
use App\DTOs\DTOtasks\TaskDto;
use Doctrine\ORM\EntityManagerInterface;
use App\MessageProducer\TaskEventPublisher;
use App\DTOs\TaskCompletedEventDTO;
use App\Repository\EpicRepository;
use App\Repository\PartRepository;
use App\Repository\LevelRepository;
use App\Repository\TaskRepository;

class TaskService
{
    public function __construct(
        private EntityManagerInterface $em,
        private EpicRepository $epicRepo,
        private PartRepository $partRepo,
        private LevelRepository $levelRepo,
        private TaskRepository $taskRepo,
        private TaskEventPublisher $taskEventPublisher
    ) {}

    // ================= CREATE =================
    public function create(TaskDto $dto): Task
    {
        $task = new Task();

        $task->setTitle($dto->title ?? 'Untitled Task');
        $task->setDescription($dto->description ?? '');
        $task->setStatus($dto->status ?? 'To Do');
        $task->setDueDate(
            $dto->dueDate ? new \DateTime($dto->dueDate) : new \DateTime()
        );

        $task->setCreatedAt(new \DateTimeImmutable());
        $task->setUpdatedAt(new \DateTimeImmutable());

        $this->setRelations($task, $dto);

        $this->em->persist($task);
        $this->em->flush();

        return $task;
    }

    // ================= UPDATE =================
    public function update(int $id, TaskDto $dto): Task
    {
        $task = $this->taskRepo->find($id);

        if (!$task) {
            throw new \Exception("Task not found (ID=$id)");
        }

        $oldStatus = $task->getStatus();

        if ($dto->title !== null) $task->setTitle($dto->title);
        if ($dto->description !== null) $task->setDescription($dto->description);
        if ($dto->status !== null) $task->setStatus($dto->status);
        if ($dto->dueDate !== null) {
            $task->setDueDate(new \DateTime($dto->dueDate));
        }

        $this->setRelations($task, $dto);

        $task->setUpdatedAt(new \DateTimeImmutable());

        $this->em->flush();

        if ($oldStatus !== 'Done' && $task->getStatus() === 'Done') {
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

        return $task;
    }

    // ================= GET ALL =================
    public function getAll(): array
    {
        return $this->taskRepo->findBy(['deletedAt' => null]);
    }

    // ================= GET BY ID (FIX 404 SAFE) =================
    public function getById(int $id): Task
    {
        $task = $this->taskRepo->findOneBy([
            'id' => $id,
            'deletedAt' => null
        ]);

        if (!$task) {
            throw new \Exception("Task not found");
        }

        return $task;
    }

    // ================= SOFT DELETE =================
    public function softDelete(int $id): Task
    {
        $task = $this->taskRepo->find($id);

        if (!$task) {
            throw new \Exception("Task not found");
        }

        $task->setDeletedAt(new \DateTime());
        $this->em->flush();

        return $task;
    }

    // ================= DASHBOARD (SAFE VERSION) =================
    public function getDashboardStats(): array
    {
        return [
            'total' => $this->taskRepo->count([]),
            'in_progress' => $this->taskRepo->count(['status' => 'In Progress']),
            'completed' => $this->taskRepo->count(['status' => 'Done']),
            'overdue' => $this->taskRepo->createQueryBuilder('t')
                ->select('COUNT(t.id)')
                ->where('t.dueDate < :now')
                ->andWhere('t.status != :done')
                ->setParameter('now', new \DateTime())
                ->setParameter('done', 'Done')
                ->getQuery()
                ->getSingleScalarResult(),
        ];
    }

    // ================= RELATIONS CLEAN =================
    private function setRelations(Task $task, TaskDto $dto): void
    {
        if ($dto->assignedTo) {
            $task->setAssignedToUserId((int)$dto->assignedTo);
        }

        if ($dto->assignedBy) {
            $task->setAssignedByUserId((int)$dto->assignedBy);
        }

        if ($dto->epic) {
            $task->setEpic($this->epicRepo->find($dto->epic));
        }

        if ($dto->part) {
            $task->setPart($this->partRepo->find($dto->part));
        }

        if ($dto->level) {
            $task->setLevel($this->levelRepo->find($dto->level));
        }
    }
}
