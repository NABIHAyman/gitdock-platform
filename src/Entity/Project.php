<?php

namespace App\Entity;

use App\Repository\ProjectRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ProjectRepository::class)]
class Project
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column]
    private ?int $Id_project = null;

    #[ORM\Column(length: 255)]
    private ?string $name = null;

    #[ORM\Column(length: 255)]
    private ?string $description = null;

    #[ORM\Column(length: 255)]
    private ?string $repoId = null;

    #[ORM\Column(length: 255)]
    private ?string $url = null;

    #[ORM\Column(length: 255)]
    private ?string $platform = null;

    #[ORM\Column]
    private ?\DateTimeImmutable $createdAt = null;

    #[ORM\Column]
    private ?\DateTimeImmutable $updatedAt = null;

    #[ORM\ManyToOne(inversedBy: 'projects')]
    #[ORM\JoinColumn(nullable: false)]
    private ?User $managedBy = null;

    /**
     * @var Collection<int, Part>
     */
    #[ORM\OneToMany(targetEntity: Part::class, mappedBy: 'project')]
    private Collection $parts;

    /**
     * @var Collection<int, Level>
     */
    #[ORM\OneToMany(targetEntity: Level::class, mappedBy: 'project')]
    private Collection $levels;

    /**
     * @var Collection<int, Epic>
     */
    #[ORM\OneToMany(targetEntity: Epic::class, mappedBy: 'project')]
    private Collection $epics;

    /**
     * @var Collection<int, Commit>
     */
    #[ORM\OneToMany(targetEntity: Commit::class, mappedBy: 'project')]
    private Collection $commits;

    public function __construct()
    {
        $this->parts = new ArrayCollection();
        $this->levels = new ArrayCollection();
        $this->epics = new ArrayCollection();
        $this->commits = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getIdProject(): ?int
    {
        return $this->Id_project;
    }

    public function setIdProject(int $Id_project): static
    {
        $this->Id_project = $Id_project;

        return $this;
    }

    public function getName(): ?string
    {
        return $this->name;
    }

    public function setName(string $name): static
    {
        $this->name = $name;

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

    public function getRepoId(): ?string
    {
        return $this->repoId;
    }

    public function setRepoId(string $repoId): static
    {
        $this->repoId = $repoId;

        return $this;
    }

    public function getUrl(): ?string
    {
        return $this->url;
    }

    public function setUrl(string $url): static
    {
        $this->url = $url;

        return $this;
    }

    public function getPlatform(): ?string
    {
        return $this->platform;
    }

    public function setPlatform(string $platform): static
    {
        $this->platform = $platform;

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

    public function getManagedBy(): ?User
    {
        return $this->managedBy;
    }

    public function setManagedBy(?User $managedBy): static
    {
        $this->managedBy = $managedBy;

        return $this;
    }

    /**
     * @return Collection<int, Part>
     */
    public function getParts(): Collection
    {
        return $this->parts;
    }

    public function addPart(Part $part): static
    {
        if (!$this->parts->contains($part)) {
            $this->parts->add($part);
            $part->setProject($this);
        }

        return $this;
    }

    public function removePart(Part $part): static
    {
        if ($this->parts->removeElement($part)) {
            // set the owning side to null (unless already changed)
            if ($part->getProject() === $this) {
                $part->setProject(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, Level>
     */
    public function getLevels(): Collection
    {
        return $this->levels;
    }

    public function addLevel(Level $level): static
    {
        if (!$this->levels->contains($level)) {
            $this->levels->add($level);
            $level->setProject($this);
        }

        return $this;
    }

    public function removeLevel(Level $level): static
    {
        if ($this->levels->removeElement($level)) {
            // set the owning side to null (unless already changed)
            if ($level->getProject() === $this) {
                $level->setProject(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, Epic>
     */
    public function getEpics(): Collection
    {
        return $this->epics;
    }

    public function addEpic(Epic $epic): static
    {
        if (!$this->epics->contains($epic)) {
            $this->epics->add($epic);
            $epic->setProject($this);
        }

        return $this;
    }

    public function removeEpic(Epic $epic): static
    {
        if ($this->epics->removeElement($epic)) {
            // set the owning side to null (unless already changed)
            if ($epic->getProject() === $this) {
                $epic->setProject(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, Commit>
     */
    public function getCommits(): Collection
    {
        return $this->commits;
    }

    public function addCommit(Commit $commit): static
    {
        if (!$this->commits->contains($commit)) {
            $this->commits->add($commit);
            $commit->setProject($this);
        }

        return $this;
    }

    public function removeCommit(Commit $commit): static
    {
        if ($this->commits->removeElement($commit)) {
            // set the owning side to null (unless already changed)
            if ($commit->getProject() === $this) {
                $commit->setProject(null);
            }
        }

        return $this;
    }
}
