<?php
namespace App\MessageHandler;

use App\Entity\User;
use App\Message\CollaboratorAddedMessage;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Messenger\Attribute\AsMessageHandler;

#[AsMessageHandler]
class CollaboratorAddedHandler
{
    public function __construct(
        private EntityManagerInterface $entityManager
    ) {}

    public function __invoke(CollaboratorAddedMessage $message): void
    {
        $existing = $this->entityManager->getRepository(User::class)
            ->findOneBy(['externalId' => (string) $message->getUserId()]);

        if ($existing) return;

        $user = new User();
        $user->setExternalId((string) $message->getUserId());
        $user->setEmail('user_' . $message->getUserId() . '@gitdock.local');
        $user->setFirstName('User');
        $user->setLastName((string) $message->getUserId());
        $user->setPassword('$2y$13$dummy');
        $user->setRoles(['ROLE_USER']);

        $this->entityManager->persist($user);
        $this->entityManager->flush();
    }
}
