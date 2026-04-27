<?php

namespace App\Repository;

use App\Entity\Task;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class TaskRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Task::class);
    }

    // =========================
    // TASKS NON SUPPRIMÉES
    // =========================

    public function findNotDeleted(): array
    {
        return $this->createQueryBuilder('t')
            ->andWhere('t.deletedAt IS NULL')
            ->orderBy('t.id', 'DESC')
            ->getQuery()
            ->getResult();
    }

    // =========================
    // STATS DASHBOARD
    // =========================

    // TOTAL TASKS
    public function countTotalTasks(): int
    {
        return (int) $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->andWhere('t.deletedAt IS NULL')
            ->getQuery()
            ->getSingleScalarResult();
    }

    // IN PROGRESS
    public function countInProgress(): int
    {
        return (int) $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->andWhere('t.status = :status')
            ->andWhere('t.deletedAt IS NULL')
            ->setParameter('status', 'In Progress')
            ->getQuery()
            ->getSingleScalarResult();
    }

    // COMPLETED
    public function countCompleted(): int
    {
        return (int) $this->createQueryBuilder('t')
            ->select('COUNT(t.id)')
            ->andWhere('t.status = :status')
            ->andWhere('t.deletedAt IS NULL')
            ->setParameter('status', 'Done')
            ->getQuery()
            ->getSingleScalarResult();
    }

    // OVERDUE
   public function countOverdue(): int
{
    return (int) $this->createQueryBuilder('t')
        ->select('COUNT(t.id)')
        ->where('t.dueDate IS NOT NULL')
        ->andWhere('t.dueDate < :now')
        ->andWhere('t.status != :done')
        ->andWhere('t.deletedAt IS NULL')
        ->setParameter('now', new \DateTime())
        ->setParameter('done', 'Done')
        ->getQuery()
        ->getSingleScalarResult();
}
}
