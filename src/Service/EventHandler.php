<?php

namespace App\Service;

use App\Entity\User;
use Doctrine\ORM\EntityManagerInterface;

class EventHandler
{
    public function __construct(
        private EntityManagerInterface $em
    ) {}

    public function handle(string $type, array $data): void
    {
        switch ($type) {

            case 'USER_CREATED':
                $this->createUser($data);
                break;

            case 'USER_UPDATED':
                $this->updateUser($data);
                break;

            case 'USER_DELETED':
                $this->deleteUser($data);
                break;

            case 'COMPANY_CREATED':
                // future logic
                break;
        }
    }

    private function createUser(array $data): void
    {
        $user = new User();

        $user->setExternalId($data['id'] ?? null);
        $user->setEmail($data['email'] ?? null);
        $user->setUsername($data['username'] ?? null);
        $user->setFirstName($data['firstName'] ?? null);
        $user->setLastName($data['lastName'] ?? null);
        $user->setEnabled($data['enabled'] ?? true);
        $user->setCompanyId($data['companyId'] ?? null);

        $this->em->persist($user);
        $this->em->flush();
    }

    private function updateUser(array $data): void
    {
        $user = $this->em->getRepository(User::class)
            ->findOneBy(['externalId' => $data['id'] ?? null]);

        if (!$user) return;

        $user->setEmail($data['email'] ?? $user->getEmail());
        $user->setUsername($data['username'] ?? $user->getUsername());
        $user->setFirstName($data['firstName'] ?? $user->getFirstName());
        $user->setLastName($data['lastName'] ?? $user->getLastName());

        $this->em->flush();
    }

    private function deleteUser(array $data): void
    {
        $user = $this->em->getRepository(User::class)
            ->findOneBy(['externalId' => $data['id'] ?? null]);

        if ($user) {
            $this->em->remove($user);
            $this->em->flush();
        }
    }
}