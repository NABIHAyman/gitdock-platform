<?php

namespace App\Entity;

use App\Repository\BranchRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: BranchRepository::class)]
class Branch
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    /**
     * @var Collection<int, Commit>
     */
    #[ORM\OneToMany(targetEntity: Commit::class, mappedBy: 'branch')]
    private Collection $commits;

    public function __construct()
    {
        $this->commits = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
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
            $commit->setBranch($this);
        }

        return $this;
    }

    public function removeCommit(Commit $commit): static
    {
        if ($this->commits->removeElement($commit)) {
            // set the owning side to null (unless already changed)
            if ($commit->getBranch() === $this) {
                $commit->setBranch(null);
            }
        }

        return $this;
    }
}
