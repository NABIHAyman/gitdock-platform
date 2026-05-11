<?php

namespace App\MessageHandler;

use App\Entity\User;
use App\Message\UserRegisteredMessage;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Messenger\Attribute\AsMessageHandler;

#[AsMessageHandler]
class UserRegisteredHandler
{
    public function __construct(private EntityManagerInterface $entityManager) {}

    public function __invoke(UserRegisteredMessage $message)
    {
        // On cherche par l'ID venant de Spring Boot (externalId)
        $user = $this->entityManager->getRepository(User::class)
            ->findOneBy(['externalId' => $message->getId()]);

        if (!$user) {
            $user = new User();
            $user->setExternalId($message->getId());
            $user->setEmail($message->getEmail());

            // On sépare le fullName s'il arrive en un seul bloc (optionnel)
            $nameParts = explode(' ', $message->getFullName(), 2);
            $user->setFirstName($nameParts[0] ?? '');
            $user->setLastName($nameParts[1] ?? '');

            // Username par défaut (utile pour Symfony Security)
            $user->setUsername($message->getEmail());

            $this->entityManager->persist($user);
            $this->entityManager->flush();
        }
    }
}
