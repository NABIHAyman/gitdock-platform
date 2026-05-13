<?php

namespace App\Entity;

use App\Repository\UserRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\UserInterface;

#[ORM\Entity(repositoryClass: UserRepository::class)]
#[ORM\HasLifecycleCallbacks]
#[ORM\Table(name: "user")]
class User implements UserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    // ID venant de Spring Boot
    #[ORM\Column(unique: true)]
    private ?int $externalId = null;

    #[ORM\Column(length: 255)]
    private ?string $username = null;

    #[ORM\Column(length: 255)]
    private ?string $email = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $firstName = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $lastName = null;

    // 🔐 ROLES (OBLIGATOIRE POUR SYMFONY SECURITY)
    #[ORM\Column]
    private array $roles = [];

    #[ORM\Column(options: ["default" => false])]
    private bool $accountLocked = false;

    #[ORM\Column(options: ["default" => true])]
    private bool $isEnabled = true;

    #[ORM\Column(options: ["default" => false])]
    private bool $isDeleted = false;

    #[ORM\Column]
    private ?\DateTimeImmutable $createdAt = null;

    #[ORM\Column]
    private ?\DateTimeImmutable $updatedAt = null;

    public function __construct()
    {
        $this->createdAt = new \DateTimeImmutable();
        $this->updatedAt = new \DateTimeImmutable();
    }

    // ================= SYMFONY SECURITY =================

    public function getUserIdentifier(): string
    {
        return $this->email;
    }

    public function getRoles(): array
    {
        $roles = $this->roles;
        $roles[] = 'ROLE_USER'; // rôle par défaut obligatoire

        return array_unique($roles);
    }

    public function setRoles(array $roles): self
    {
        $this->roles = $roles;
        return $this;
    }

    public function eraseCredentials(): void
    {
        // rien (pas de password ici)
    }

    // ================= LIFECYCLE =================

    #[ORM\PreUpdate]
    public function preUpdate(): void
    {
        $this->updatedAt = new \DateTimeImmutable();
    }

    // ================= GETTERS / SETTERS =================

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getExternalId(): ?int
    {
        return $this->externalId;
    }

    public function setExternalId(int $externalId): self
    {
        $this->externalId = $externalId;
        return $this;
    }

    public function getUsername(): ?string
    {
        return $this->username;
    }

    public function setUsername(string $username): self
    {
        $this->username = $username;
        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): self
    {
        $this->email = $email;
        return $this;
    }

    public function getFirstName(): ?string
    {
        return $this->firstName;
    }

    public function setFirstName(?string $firstName): self
    {
        $this->firstName = $firstName;
        return $this;
    }

    public function getLastName(): ?string
    {
        return $this->lastName;
    }

    public function setLastName(?string $lastName): self
    {
        $this->lastName = $lastName;
        return $this;
    }

    public function getAccountLocked(): bool
    {
        return $this->accountLocked;
    }

    public function setAccountLocked(bool $accountLocked): self
    {
        $this->accountLocked = $accountLocked;
        return $this;
    }

    public function getIsEnabled(): bool
    {
        return $this->isEnabled;
    }

    public function setIsEnabled(bool $isEnabled): self
    {
        $this->isEnabled = $isEnabled;
        return $this;
    }

    public function getIsDeleted(): bool
    {
        return $this->isDeleted;
    }

    public function setIsDeleted(bool $isDeleted): self
    {
        $this->isDeleted = $isDeleted;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeImmutable
    {
        return $this->createdAt;
    }

    public function getUpdatedAt(): ?\DateTimeImmutable
    {
        return $this->updatedAt;
    }

    // helper UI
    public function getFullName(): string
    {
        return trim(($this->firstName ?? '') . ' ' . ($this->lastName ?? ''));
    }
}