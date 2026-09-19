<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260505161322 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE epic DROP id_epic, CHANGE project_id project_id INT NOT NULL');
        $this->addSql('ALTER TABLE level DROP id_level, DROP type, DROP level_rank, DROP scope, DROP created_at, DROP updated_at, CHANGE name name VARCHAR(50) NOT NULL');
        $this->addSql('ALTER TABLE task ADD priority VARCHAR(255) NOT NULL, ADD completed_at DATETIME DEFAULT NULL, ADD part_id INT DEFAULT NULL, CHANGE title title VARCHAR(255) DEFAULT NULL, CHANGE description description VARCHAR(255) DEFAULT NULL, CHANGE status status VARCHAR(255) NOT NULL, CHANGE due_date due_date DATETIME DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE epic ADD id_epic INT NOT NULL, CHANGE project_id project_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE task DROP priority, DROP completed_at, DROP part_id, CHANGE title title VARCHAR(255) NOT NULL, CHANGE description description VARCHAR(255) NOT NULL, CHANGE status status VARCHAR(50) NOT NULL, CHANGE due_date due_date DATETIME NOT NULL');
        $this->addSql('ALTER TABLE level ADD id_level INT NOT NULL, ADD type VARCHAR(255) NOT NULL, ADD level_rank VARCHAR(255) NOT NULL, ADD scope VARCHAR(255) NOT NULL, ADD created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', ADD updated_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', CHANGE name name VARCHAR(255) NOT NULL');
    }
}
