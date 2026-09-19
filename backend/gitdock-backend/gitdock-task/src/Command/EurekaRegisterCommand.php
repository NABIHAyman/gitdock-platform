<?php
namespace App\Command;

use App\Service\EurekaClient;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

#[AsCommand(name: 'app:eureka:register', description: 'Enregistre le service sur Eureka et envoie les heartbeats')]
class EurekaRegisterCommand extends Command
{
    public function __construct(private EurekaClient $eurekaClient)
    {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $output->writeln("🚀 Lancement du protocole d'enregistrement Eureka...");

        // 1. Premier enregistrement
        $this->eurekaClient->register();
        $output->writeln("📡 Service envoyé à la base (Eureka). Début des Heartbeats (30s)...");

        // 2. Boucle infinie pour maintenir la connexion en vie
        while (true) {
            sleep(30); // Standard Eureka : 30 secondes
            $this->eurekaClient->heartbeat();
        }

        return Command::SUCCESS;
    }
}
