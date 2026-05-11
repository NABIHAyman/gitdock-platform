<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260509004507 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE level DROP project_id, CHANGE name name VARCHAR(255) NOT NULL');
        $this->addSql('DROP INDEX UNIQ_8D93D6499F75D7B0 ON user');
        $this->addSql('ALTER TABLE user DROP first_name, DROP last_name, DROP account_locked, DROP is_enabled, DROP is_deleted, DROP created_at, DROP updated_at, CHANGE external_id external_id VARCHAR(255) DEFAULT NULL, CHANGE email email VARCHAR(180) NOT NULL, CHANGE username password VARCHAR(255) NOT NULL');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_8D93D649E7927C74 ON user (email)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('DROP INDEX UNIQ_8D93D649E7927C74 ON `user`');
        $this->addSql('ALTER TABLE `user` ADD first_name VARCHAR(255) DEFAULT NULL, ADD last_name VARCHAR(255) DEFAULT NULL, ADD account_locked TINYINT(1) DEFAULT 0 NOT NULL, ADD is_enabled TINYINT(1) DEFAULT 1 NOT NULL, ADD is_deleted TINYINT(1) DEFAULT 0 NOT NULL, ADD created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', ADD updated_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', CHANGE email email VARCHAR(255) NOT NULL, CHANGE external_id external_id INT NOT NULL, CHANGE password username VARCHAR(255) NOT NULL');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_8D93D6499F75D7B0 ON `user` (external_id)');
        $this->addSql('ALTER TABLE level ADD project_id INT DEFAULT NULL, CHANGE name name VARCHAR(50) NOT NULL');
    }
}
