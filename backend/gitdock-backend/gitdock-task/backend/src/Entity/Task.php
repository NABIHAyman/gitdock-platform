<?php

namespace App\Entity;

use App\Repository\TaskRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TaskRepository::class)]
class Task
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private ?string $title = null;

    #[ORM\Column(length: 255)]
    private ?string $description = null;

    #[ORM\Column(length: 255)]
    private ?string $status = null;

    #[ORM\Column(type: 'datetime')]
    private ?\DateTime $dueDate = null;

    #[ORM\Column(type: 'datetime_immutable')]
    private ?\DateTimeImmutable $createdAt = null;

    #[ORM\Column(type: 'datetime_immutable')]
    private ?\DateTimeImmutable $updatedAt = null;

    #[ORM\ManyToOne(inversedBy: 'tasks')]
    private ?Epic $epic = null;

    #[ORM\ManyToOne(inversedBy: 'tasks')]
    private ?Part $part = null;

    #[ORM\ManyToOne(inversedBy: 'tasks')]
    private ?Level $level = null;

    #[ORM\ManyToOne(inversedBy: 'tasks')]
    private ?TaskLevel $taskLevel = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $projectId = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $assignedToUserId = null;

    #[ORM\Column(type: 'integer', nullable: true)]
    private ?int $assignedByUserId = null;

    // ✅ SOFT DELETE
    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTime $deletedAt = null;



    // ---------------- GETTERS / SETTERS ----------------

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTitle(): ?string
    {
        return $this->title;
    }

    public function setTitle(string $title): static
    {
        $this->title = $title;
        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(string $description): static
    {
        $this->description = $description;
        return $this;
    }

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(string $status): static
    {
        $this->status = $status;
        return $this;
    }

    public function getDueDate(): ?\DateTime
    {
        return $this->dueDate;
    }

    public function setDueDate(\DateTime $dueDate): static
    {
        $this->dueDate = $dueDate;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeImmutable
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeImmutable $createdAt): static
    {
        $this->createdAt = $createdAt;
        return $this;
    }

    public function getUpdatedAt(): ?\DateTimeImmutable
    {
        return $this->updatedAt;
    }

    public function setUpdatedAt(\DateTimeImmutable $updatedAt): static
    {
        $this->updatedAt = $updatedAt;
        return $this;
    }

    public function getEpic(): ?Epic
    {
        return $this->epic;
    }

    public function setEpic(?Epic $epic): static
    {
        $this->epic = $epic;
        return $this;
    }

    public function getPart(): ?Part
    {
        return $this->part;
    }

    public function setPart(?Part $part): static
    {
        $this->part = $part;
        return $this;
    }

    public function getLevel(): ?Level
    {
        return $this->level;
    }

    public function setLevel(?Level $level): static
    {
        $this->level = $level;
        return $this;
    }

    public function getTaskLevel(): ?TaskLevel
    {
        return $this->taskLevel;
    }

    public function setTaskLevel(?TaskLevel $taskLevel): static
    {
        $this->taskLevel = $taskLevel;
        return $this;
    }

    public function getProjectId(): ?int
    {
        return $this->projectId;
    }

    public function setProjectId(?int $projectId): static
    {
        $this->projectId = $projectId;
        return $this;
    }

    public function getAssignedToUserId(): ?int
    {
        return $this->assignedToUserId;
    }

    public function setAssignedToUserId(?int $assignedToUserId): static
    {
        $this->assignedToUserId = $assignedToUserId;
        return $this;
    }

    public function getAssignedByUserId(): ?int
    {
        return $this->assignedByUserId;
    }

    public function setAssignedByUserId(?int $assignedByUserId): static
    {
        $this->assignedByUserId = $assignedByUserId;
        return $this;
    }

    // ---------------- SOFT DELETE ----------------

    public function getDeletedAt(): ?\DateTime
    {
        return $this->deletedAt;
    }

    public function setDeletedAt(?\DateTime $deletedAt): static
    {
        $this->deletedAt = $deletedAt;
        return $this;
    }

    public function isDeleted(): bool
    {
        return $this->deletedAt !== null;
    }
}
