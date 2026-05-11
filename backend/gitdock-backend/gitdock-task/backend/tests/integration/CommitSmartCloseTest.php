<?php

namespace App\Tests\Integration;

use App\DTOs\CommitSavedEventDTO;
use App\Entity\Task;
use Symfony\Bundle\FrameworkBundle\Test\KernelTestCase;
use Symfony\Component\Messenger\MessageBusInterface;

class CommitSmartCloseTest extends KernelTestCase
{
    public function testTaskStatutChangesToDoneOnFixesCommit(): void
    {
        // 1. DÉFINITION DES VARIABLES D'ENV (AVANT LE KERNEL)
        putenv('MESSENGER_TRANSPORT_DSN=sync://');
        $_ENV['MESSENGER_TRANSPORT_DSN'] = 'sync://';

        // 2. BOOT DU KERNEL ET RÉCUPÉRATION DES SERVICES
        self::bootKernel();
        $container = static::getContainer();
        $em = $container->get('doctrine')->getManager();
        $bus = $container->get(MessageBusInterface::class);

        // 3. CRÉATION D'UNE TÂCHE EN BDD
        $task = new Task();
        $task->setTitle("Apprendre RabbitMQ");
        $task->setDescription("Description de la tache");
        $task->setStatus("Todo");
        $task->setProjectId(1);
        $task->setCreatedAt(new \DateTimeImmutable());
        $task->setUpdatedAt(new \DateTimeImmutable());
        $task->setDueDate(new \DateTime());

        $em->persist($task);
        $em->flush();

        $taskId = $task->getId();

        // 4. SIMULATION DU MESSAGE COMMIT
        $message = new CommitSavedEventDTO(
            "dev@gitdock.com",
            "Fixes #$taskId - Fin de la tâche",
            1,
            123
        );

        // Dispatch synchrone (exécute le Handler immédiatement)
        $bus->dispatch($message);

        // 5. RAFRAÎCHISSEMENT ET ASSERTION
        $em->clear(); // On vide le cache de Doctrine pour forcer une lecture en BDD
        $updatedTask = $em->getRepository(Task::class)->find($taskId);

        $this->assertEquals('Done', $updatedTask->getStatus(), "La tâche devrait passer à 'Done' après le message.");

        echo "\n ✅ Test réussi : La tâche #$taskId est passée de Todo à Done via le bus synchrone ! \n";
    }
}
