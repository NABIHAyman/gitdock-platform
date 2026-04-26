<?php

namespace App\Entity;

use App\Repository\TaskRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TaskRepository::class)]
#[ORM\HasLifecycleCallbacks]
class Task
{
    // ================= PRIMARY KEY =================
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    // ================= BASIC DATA =================
    #[ORM\Column(length: 255)]
    private ?string $title = null;

    #[ORM\Column(length: 255)]
    private ?string $description = null;

    #[ORM\Column(length: 50)]
    private string $status = 'To Do';

    #[ORM\Column(type: 'datetime')]
    private ?\DateTime $dueDate = null;

    // ================= MICROSERVICE RELATIONS (ONLY IDS) =================
    #[ORM\Column(nullable: true)]
    private ?int $epicId = null;

    #[ORM\Column(nullable: true)]
    private ?int $projectId = null; // venant Spring Boot

    #[ORM\Column(nullable: true)]
    private ?int $levelId = null;

    // users (Spring Boot auth sync)
    #[ORM\Column(nullable: true)]
    private ?int $assignedTo = null;

    #[ORM\Column(nullable: true)]
    private ?int $assignedBy = null;

    // ================= AUDIT =================
    #[ORM\Column(type: 'datetime_immutable')]
    private ?\DateTimeImmutable $createdAt = null;

    #[ORM\Column(type: 'datetime_immutable')]
    private ?\DateTimeImmutable $updatedAt = null;

    // ================= SOFT DELETE =================
    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTime $deletedAt = null;

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

    // ================= GETTERS / SETTERS =================

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTitle(): ?string
    {
        return $this->title;
    }

    public function setTitle(string $title): self
    {
        $this->title = $title;
        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function getStatus(): string
    {
        return $this->status;
    }

    public function setStatus(string $status): self
    {
        $this->status = $status;
        return $this;
    }

    public function getDueDate(): ?\DateTime
    {
        return $this->dueDate;
    }

    public function setDueDate(\DateTime $dueDate): self
    {
        $this->dueDate = $dueDate;
        return $this;
    }

    // ================= IDS =================

    public function getEpicId(): ?int
    {
        return $this->epicId;
    }

    public function setEpicId(?int $epicId): self
    {
        $this->epicId = $epicId;
        return $this;
    }

    public function getProjectId(): ?int
    {
        return $this->projectId;
    }

    public function setProjectId(?int $projectId): self
    {
        $this->projectId = $projectId;
        return $this;
    }

    public function getLevelId(): ?int
    {
        return $this->levelId;
    }

    public function setLevelId(?int $levelId): self
    {
        $this->levelId = $levelId;
        return $this;
    }

    public function getAssignedTo(): ?int
    {
        return $this->assignedTo;
    }

    public function setAssignedTo(?int $assignedTo): self
    {
        $this->assignedTo = $assignedTo;
        return $this;
    }

    public function getAssignedBy(): ?int
    {
        return $this->assignedBy;
    }

    public function setAssignedBy(?int $assignedBy): self
    {
        $this->assignedBy = $assignedBy;
        return $this;
    }

    // ================= SOFT DELETE =================

    public function getDeletedAt(): ?\DateTime
    {
        return $this->deletedAt;
    }

    public function setDeletedAt(?\DateTime $deletedAt): self
    {
        $this->deletedAt = $deletedAt;
        return $this;
    }

    public function isDeleted(): bool
    {
        return $this->deletedAt !== null;
    }
}