<?php

namespace App\Service;

use App\Entity\Task;
use App\Repository\TaskRepository;
use Doctrine\ORM\EntityManagerInterface;
use App\Message\TaskCompletedEvent;
use App\Message\Producer\TaskEventProducer;
use App\DTOs\DTOtasks\TaskDto;
use App\Enum\TaskStatus;
use App\Enum\TaskPriority;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use App\Client\AuthClientInterface;

class TaskService
{
    private array $userCache = []; // 🔥 cache simple

    public function __construct(
        private EntityManagerInterface $em,
        private TaskRepository $repo,
        private TaskEventProducer $taskEventProducer,
        private AuthClientInterface $authClient
    ) {}

    // ================= CREATE =================
    public function create(TaskDto $dto): Task
    {
        $task = new Task();

        $task->setTitle($dto->title)
            ->setDescription($dto->description)
            ->setStatus($dto->status ? TaskStatus::from($dto->status) : TaskStatus::TODO)
            ->setPriority($dto->priority ? TaskPriority::from($dto->priority) : TaskPriority::MEDIUM)
            ->setDueDate($dto->dueDate ? new \DateTime($dto->dueDate) : null)
            ->setAssignedTo($dto->assignedTo)
            ->setAssignedBy($dto->assignedBy)
            ->setEpicId($dto->epicId)
            ->setLevelId($dto->levelId)
            ->setPartId($dto->partId)
            ->setProjectId($dto->projectId);

        $this->em->persist($task);
        $this->em->flush();

        return $task;
    }

    // ================= GET ALL =================
    public function getAll(): array
    {
        $tasks = $this->repo->findNotDeleted();

        // 🔥 extract user IDs
        $userIds = array_values(array_unique(array_filter(array_map(
            fn($task) => $task->getAssignedTo(),
            $tasks
        ))));

        // 🔥 fetch users from Spring (batch)
        $users = $this->authClient->getUsersByIds($userIds);

        // 🔥 map users by ID
        $usersMap = [];
        foreach ($users as $user) {
            $usersMap[$user['id']] = $user;
        }

        // 🔥 build response
        $result = [];

        foreach ($tasks as $task) {
            $assignedId = $task->getAssignedTo();

            $result[] = [
                'id' => $task->getId(),
                'title' => $task->getTitle(),
                'description' => $task->getDescription(),
                'status' => $task->getStatus()->value,
                'priority' => $task->getPriority()->value,
                'assignedTo' => $assignedId,
                'assignedUser' => $usersMap[$assignedId] ?? null
            ];
        }

        return $result;
    }

    // ================= GET BY ID =================
    public function getById(int $id): array
    {
        $task = $this->getTaskEntity($id);

        $user = null;
        $userId = $task->getAssignedTo();

        if ($userId) {

            // 🔥 cache optimization
            if (!isset($this->userCache[$userId])) {
                $users = $this->authClient->getUsersByIds([$userId]);
                $this->userCache[$userId] = $users[0] ?? null;
            }

            $user = $this->userCache[$userId];
        }

        return [
            'id' => $task->getId(),
            'title' => $task->getTitle(),
            'description' => $task->getDescription(),
            'status' => $task->getStatus()->value,
            'priority' => $task->getPriority()->value,
            'assignedUser' => $user
        ];
    }

    // ================= UPDATE =================
    public function update(int $id, TaskDto $dto): Task
    {
        $task = $this->getTaskEntity($id);

        if ($dto->title !== null) {
            $task->setTitle($dto->title);
        }

        if ($dto->description !== null) {
            $task->setDescription($dto->description);
        }

        if ($dto->status !== null) {
            $task->setStatus(TaskStatus::from($dto->status));
        }

        if ($dto->priority !== null) {
            $task->setPriority(TaskPriority::from($dto->priority));
        }

        if ($dto->dueDate !== null) {
            $task->setDueDate(new \DateTime($dto->dueDate));
        }

        $this->em->flush();

        return $task;
    }

    // ================= SOFT DELETE =================
    public function softDelete(int $id): void
    {
        $task = $this->getTaskEntity($id);

        $task->setDeletedAt(new \DateTime());
        $this->em->flush();
    }

    // ================= MARK AS DONE =================
    public function markAsDone(int $id): Task
    {
        $task = $this->getTaskEntity($id);

        if (!$task->getAssignedTo()) {
            throw new \DomainException("Task must have assigned user");
        }

        if ($task->getStatus() === TaskStatus::DONE) {
            return $task;
        }

        $task->setStatus(TaskStatus::DONE);
        $task->setCompletedAt(new \DateTime());

        $xp = max(1, match ($task->getLevelId()) {
            1 => 10,
            2 => 20,
            3 => 50,
            default => 10
        });

        $event = new TaskCompletedEvent(
            $task->getId(),
            $task->getAssignedTo(),
            $task->getLevelId(),
            $xp
        );

        $this->em->flush();

        $this->taskEventProducer->publishTaskCompleted($event);

        return $task;
    }

    // ================= PRIVATE =================
    private function getTaskEntity(int $id): Task
    {
        $task = $this->repo->find($id);

        if (!$task || $task->isDeleted()) {
            throw new NotFoundHttpException("Task not found");
        }

        return $task;
    }
}
