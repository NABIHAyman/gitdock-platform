<?php

namespace App\Entity;

use App\Enum\TaskStatus;
use App\Enum\TaskPriority;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\HasLifecycleCallbacks]
class Task
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $title = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $description = null;

    // ✅ ENUM STATUS
    #[ORM\Column(enumType: TaskStatus::class)]
    private TaskStatus $status;

    // ✅ ENUM PRIORITY
    #[ORM\Column(enumType: TaskPriority::class)]
    private TaskPriority $priority;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $dueDate = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $completedAt = null;

    #[ORM\Column(type: 'datetime_immutable')]
    private ?\DateTimeImmutable $createdAt = null;

    #[ORM\Column(type: 'datetime_immutable')]
    private ?\DateTimeImmutable $updatedAt = null;

    #[ORM\Column(nullable: true)]
    private ?int $epicId = null;

    #[ORM\Column(nullable: true)]
    private ?int $projectId = null;

    #[ORM\Column(nullable: true)]
    private ?int $levelId = null;

    #[ORM\Column(nullable: true)]
    private ?int $partId = null;

    #[ORM\Column(nullable: true)]
    private ?int $assignedTo = null;

    #[ORM\Column(nullable: true)]
    private ?int $assignedBy = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $deletedAt = null;

    // ================= CONSTRUCTOR =================
    public function __construct()
    {
        $this->status = TaskStatus::TODO;
        $this->priority = TaskPriority::MEDIUM;
    }

    // ================= LIFECYCLE =================
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

    // ================= GETTERS =================
    public function getId(): ?int { return $this->id; }

    public function getStatus(): TaskStatus
    {
        return $this->status;
    }

    public function getPriority(): TaskPriority
    {
        return $this->priority;
    }

    public function getLevelId(): ?int { return $this->levelId; }
    public function getAssignedTo(): ?int { return $this->assignedTo; }

    public function getCompletedAt(): ?\DateTimeInterface
    {
        return $this->completedAt;
    }

    public function isDeleted(): bool
    {
        return $this->deletedAt !== null;
    }

    // ================= SETTERS =================
    public function setTitle(?string $title): self
    {
        $this->title = $title;
        return $this;
    }

    public function setDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function setStatus(TaskStatus $status): self
    {
        $this->status = $status;
        return $this;
    }

    public function setPriority(TaskPriority $priority): self
    {
        $this->priority = $priority;
        return $this;
    }

    public function setDueDate(?\DateTimeInterface $dueDate): self
    {
        $this->dueDate = $dueDate;
        return $this;
    }

    public function setCompletedAt(?\DateTimeInterface $completedAt): self
    {
        $this->completedAt = $completedAt;
        return $this;
    }

    public function setEpicId(?int $epicId): self
    {
        $this->epicId = $epicId;
        return $this;
    }

    public function setProjectId(?int $projectId): self
    {
        $this->projectId = $projectId;
        return $this;
    }

    public function setLevelId(?int $levelId): self
    {
        $this->levelId = $levelId;
        return $this;
    }

    public function setPartId(?int $partId): self
    {
        $this->partId = $partId;
        return $this;
    }

    public function setAssignedTo(?int $assignedTo): self
    {
        $this->assignedTo = $assignedTo;
        return $this;
    }

    public function setAssignedBy(?int $assignedBy): self
    {
        $this->assignedBy = $assignedBy;
        return $this;
    }

    public function setDeletedAt(?\DateTimeInterface $deletedAt): self
    {
        $this->deletedAt = $deletedAt;
        return $this;
    }
}
