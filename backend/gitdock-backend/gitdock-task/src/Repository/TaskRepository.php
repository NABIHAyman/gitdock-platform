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

    public function findNotDeleted(): array
    {
        return $this->createQueryBuilder('t')
            ->andWhere('t.deletedAt IS NULL')
            ->orderBy('t.id', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
