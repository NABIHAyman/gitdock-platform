<?php

namespace App\Service;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;

class UserSyncService
{
    public function __construct(
        private EntityManagerInterface $em
    ) {}

    public function sync(string $type, array $data): void
    {
        $user = $this->em->getRepository(User::class)
            ->findOneBy(['externalId' => $data['id'] ?? null]);

        // 🗑 DELETE
        if ($type === 'user.deleted') {
            if ($user) {
                $this->em->remove($user);
                $this->em->flush();
            }
            return;
        }

        // ➕ CREATE / UPDATE
        if (!$user) {
            $user = new User();
            $user->setExternalId($data['id']);
        }

        $user->setUsername($data['username'] ?? '');
        $user->setEmail($data['email'] ?? '');
        $user->setFirstName($data['firstName'] ?? null);
        $user->setLastName($data['lastName'] ?? null);

        $this->em->persist($user);
        $this->em->flush();
    }
}