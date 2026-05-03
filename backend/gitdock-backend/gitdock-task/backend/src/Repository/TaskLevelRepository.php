<?php

namespace App\Repository;

use App\Entity\TaskLevel;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<TaskLevel>
 *
 * @method TaskLevel|null find($id, $lockMode = null, $lockVersion = null)
 * @method TaskLevel|null findOneBy(array $criteria, array $orderBy = null)
 * @method TaskLevel[]    findAll()
 * @method TaskLevel[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class TaskLevelRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, TaskLevel::class);
    }
}
