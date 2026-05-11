<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260509233359 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE task ADD CONSTRAINT FK_527EDB2589EEAF91 FOREIGN KEY (assigned_to) REFERENCES `user` (id)');
        $this->addSql('CREATE INDEX IDX_527EDB2589EEAF91 ON task (assigned_to)');
        $this->addSql('ALTER TABLE user DROP external_id, DROP password, DROP roles');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE `user` ADD external_id VARCHAR(255) DEFAULT NULL, ADD password VARCHAR(255) NOT NULL, ADD roles JSON NOT NULL');
        $this->addSql('ALTER TABLE task DROP FOREIGN KEY FK_527EDB2589EEAF91');
        $this->addSql('DROP INDEX IDX_527EDB2589EEAF91 ON task');
    }
}
