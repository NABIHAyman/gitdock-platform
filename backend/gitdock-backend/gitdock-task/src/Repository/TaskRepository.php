<?php

namespace App\Repository;

use App\Entity\Task;
use App\Enum\TaskStatus;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class TaskRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Task::class);
    }

    public function findNotDeleted(): array
    {
        return $this->createQueryBuilder('t')
            ->andWhere('t.deletedAt IS NULL')
            ->orderBy('t.id', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function countTotalTasks(): int
    {
        return (int) $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->andWhere('t.deletedAt IS NULL')
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countInProgress(): int
    {
        return (int) $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->andWhere('t.status = :status')
            ->andWhere('t.deletedAt IS NULL')
            ->setParameter('status', TaskStatus::IN_PROGRESS) // ✅ FIX
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countCompleted(): int
    {
        return (int) $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->andWhere('t.status = :status')
            ->andWhere('t.deletedAt IS NULL')
            ->setParameter('status', TaskStatus::DONE) // ✅ FIX
            ->getQuery()
            ->getSingleScalarResult();
    }
}
