<?php

namespace App\Entity;

use App\Enum\TaskStatus;
use App\Enum\TaskPriority;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity]
#[ORM\HasLifecycleCallbacks]
class Task
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    #[Groups(['task:read'])]
    private ?int $id = null;

    #[ORM\Column(length: 255, nullable: true)]
    #[Groups(['task:read'])]
    private ?string $title = null;

    #[ORM\Column(length: 255, nullable: true)]
    #[Groups(['task:read'])]
    private ?string $description = null;

    #[ORM\Column(enumType: TaskStatus::class)]
    #[Groups(['task:read'])]
    private TaskStatus $status;

    #[ORM\Column(enumType: TaskPriority::class)]
    #[Groups(['task:read'])]
    private TaskPriority $priority;

    #[ORM\Column(type: 'datetime', nullable: true)]
    #[Groups(['task:read'])]
    private ?\DateTimeInterface $dueDate = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    #[Groups(['task:read'])]
    private ?\DateTimeInterface $completedAt = null;

    #[ORM\Column(type: 'datetime_immutable')]
    #[Groups(['task:read'])]
    private ?\DateTimeImmutable $createdAt = null;

    #[ORM\Column(type: 'datetime_immutable', nullable: true)]
    private ?\DateTimeImmutable $updatedAt = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: "assigned_to", referencedColumnName: "id", nullable: true)]
    #[Groups(['task:read'])]
    private ?User $assignedTo = null;

    #[ORM\Column(nullable: true)]
    #[Groups(['task:read'])]
    private ?int $projectId = null;

    #[ORM\Column(nullable: true)]
    private ?int $epicId = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $deletedAt = null;

    public function __construct()
    {
        $this->status = TaskStatus::TODO;
        $this->priority = TaskPriority::MEDIUM;
    }

    #[ORM\PrePersist]
    public function onCreate(): void
    {
        $this->createdAt = new \DateTimeImmutable();
        $this->updatedAt = new \DateTimeImmutable();
    }

    #[ORM\PreUpdate]
    public function onUpdate(): void
    {
        $this->updatedAt = new \DateTimeImmutable();
    }

    public function getId(): ?int { return $this->id; }

    public function getTitle(): ?string { return $this->title; }
    public function setTitle(?string $title): self { $this->title = $title; return $this; }

    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): self { $this->description = $description; return $this; }

    public function getStatus(): TaskStatus { return $this->status; }
    public function setStatus(TaskStatus $status): self { $this->status = $status; return $this; }

    public function getPriority(): TaskPriority { return $this->priority; }
    public function setPriority(TaskPriority $priority): self { $this->priority = $priority; return $this; }

    public function getDueDate(): ?\DateTimeInterface { return $this->dueDate; }
    public function setDueDate(?\DateTimeInterface $dueDate): self { $this->dueDate = $dueDate; return $this; }

    public function getCompletedAt(): ?\DateTimeInterface { return $this->completedAt; }
    public function setCompletedAt(?\DateTimeInterface $completedAt): self { $this->completedAt = $completedAt; return $this; }

    public function getAssignedTo(): ?User { return $this->assignedTo; }
    public function setAssignedTo(?User $assignedTo): self { $this->assignedTo = $assignedTo; return $this; }

    public function getCreatedAt(): ?\DateTimeImmutable { return $this->createdAt; }
    public function getUpdatedAt(): ?\DateTimeImmutable { return $this->updatedAt; }

    public function getProjectId(): ?int { return $this->projectId; }
    public function setProjectId(?int $projectId): self { $this->projectId = $projectId; return $this; }

    public function getDeletedAt(): ?\DateTimeInterface { return $this->deletedAt; }
    public function setDeletedAt(?\DateTimeInterface $deletedAt): self { $this->deletedAt = $deletedAt; return $this; }

    public function isDeleted(): bool { return $this->deletedAt !== null; }
}
