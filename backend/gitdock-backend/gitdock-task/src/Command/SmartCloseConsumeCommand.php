<?php

namespace App\Command;

use App\Message\AmqpConnectionFactory;
use App\Service\SmartCloseService;
use Doctrine\ORM\EntityManagerInterface;
use PhpAmqpLib\Exchange\AMQPExchangeType;
use PhpAmqpLib\Message\AMQPMessage;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

/**
 * Écoute les commits enregistrés par gitdock-project (project.commit.saved
 * sur gitdock.exchange) et applique le Smart Close.
 */
#[AsCommand(
    name: 'app:smart-close:consume',
    description: 'Smart Close : clôture les tâches citées par « Fixes #ID » dans les commits'
)]
class SmartCloseConsumeCommand extends Command
{
    private const EXCHANGE = 'gitdock.exchange';
    private const QUEUE = 'task.smart-close.queue';
    private const ROUTING_KEY = 'project.commit.saved';

    public function __construct(
        private AmqpConnectionFactory $connections,
        private SmartCloseService $smartClose,
        private EntityManagerInterface $em
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $connection = $this->connections->create();
        $channel = $connection->channel();

        // Même déclaration que gitdock-project (topic, durable)
        $channel->exchange_declare(self::EXCHANGE, AMQPExchangeType::TOPIC, false, true, false);
        $channel->queue_declare(self::QUEUE, false, true, false, false);
        $channel->queue_bind(self::QUEUE, self::EXCHANGE, self::ROUTING_KEY);
        $channel->basic_qos(null, 1, null);

        $output->writeln('Smart Close : en attente de project.commit.saved...');

        $callback = function (AMQPMessage $msg) use ($output) {
            $body = json_decode($msg->getBody(), true);

            if (!is_array($body) || !isset($body['message'], $body['projectId'])) {
                $output->writeln('Message ignoré : format inattendu');
                $msg->ack();
                return;
            }

            try {
                $task = $this->smartClose->handleCommit((string) $body['message'], (int) $body['projectId']);
                if ($task !== null) {
                    $output->writeln(sprintf(
                        'Tâche #%d clôturée par le commit %s',
                        $task->getId(),
                        $body['hash'] ?? '?'
                    ));
                }
            } catch (\Throwable $e) {
                $output->writeln('Smart Close non appliqué : ' . $e->getMessage());
            } finally {
                // Processus long : on repart d'un état Doctrine propre à chaque message
                $this->em->clear();
            }

            $msg->ack();
        };

        $channel->basic_consume(self::QUEUE, '', false, false, false, false, $callback);

        while ($channel->is_consuming()) {
            $channel->wait();
        }

        $channel->close();
        $connection->close();

        return Command::SUCCESS;
    }
}
