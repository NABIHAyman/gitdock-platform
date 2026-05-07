<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260426230304 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE task DROP FOREIGN KEY FK_527EDB254CE34BEC');
        $this->addSql('ALTER TABLE level DROP FOREIGN KEY FK_9AEACC13166D1F9C');
        $this->addSql('ALTER TABLE epic DROP FOREIGN KEY FK_19C95071166D1F9C');
        $this->addSql('ALTER TABLE part DROP FOREIGN KEY FK_490F70C6166D1F9C');
        $this->addSql('ALTER TABLE project DROP FOREIGN KEY FK_2FB3D0EE873649CA');
        $this->addSql('ALTER TABLE commit DROP FOREIGN KEY FK_4ED42EAD166D1F9C');
        $this->addSql('ALTER TABLE commit DROP FOREIGN KEY FK_4ED42EAD8DB60186');
        $this->addSql('ALTER TABLE commit DROP FOREIGN KEY FK_4ED42EADA76ED395');
        $this->addSql('ALTER TABLE commit DROP FOREIGN KEY FK_4ED42EADDCD6CC49');
        $this->addSql('DROP TABLE branch');
        $this->addSql('DROP TABLE part');
        $this->addSql('DROP TABLE project');
        $this->addSql('DROP TABLE commit');
        $this->addSql('DROP INDEX IDX_19C95071166D1F9C ON epic');
        $this->addSql('ALTER TABLE epic CHANGE project_id project_id INT DEFAULT NULL');
        $this->addSql('DROP INDEX IDX_9AEACC13166D1F9C ON level');
        $this->addSql('ALTER TABLE task DROP FOREIGN KEY FK_527EDB255FB14BA7');
        $this->addSql('ALTER TABLE task DROP FOREIGN KEY FK_527EDB256B71E00E');
        $this->addSql('ALTER TABLE task DROP FOREIGN KEY FK_527EDB256E6F1246');
        $this->addSql('ALTER TABLE task DROP FOREIGN KEY FK_527EDB25F4BD7827');
        $this->addSql('DROP INDEX IDX_527EDB256E6F1246 ON task');
        $this->addSql('DROP INDEX IDX_527EDB25F4BD7827 ON task');
        $this->addSql('DROP INDEX IDX_527EDB255FB14BA7 ON task');
        $this->addSql('DROP INDEX IDX_527EDB254CE34BEC ON task');
        $this->addSql('DROP INDEX IDX_527EDB256B71E00E ON task');
        $this->addSql('ALTER TABLE task ADD project_id INT DEFAULT NULL, ADD assigned_to INT DEFAULT NULL, ADD assigned_by INT DEFAULT NULL, DROP part_id, DROP assigned_to_id, DROP assigned_by_id, CHANGE status status VARCHAR(50) NOT NULL');
        $this->addSql('ALTER TABLE user ADD roles JSON NOT NULL, CHANGE is_enabled is_enabled TINYINT(1) DEFAULT 1 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE branch (id INT AUTO_INCREMENT NOT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE part (id INT AUTO_INCREMENT NOT NULL, project_id INT NOT NULL, id_part INT NOT NULL, name VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', updated_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', INDEX IDX_490F70C6166D1F9C (project_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE project (id INT AUTO_INCREMENT NOT NULL, managed_by_id INT NOT NULL, id_project INT NOT NULL, name VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, repo_id VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, url VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, platform VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', updated_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', INDEX IDX_2FB3D0EE873649CA (managed_by_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE commit (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, project_id INT NOT NULL, task_id INT NOT NULL, branch_id INT NOT NULL, commit_id VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, hash VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, message LONGTEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', updated_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', INDEX IDX_4ED42EADDCD6CC49 (branch_id), INDEX IDX_4ED42EAD8DB60186 (task_id), INDEX IDX_4ED42EAD166D1F9C (project_id), INDEX IDX_4ED42EADA76ED395 (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE part ADD CONSTRAINT FK_490F70C6166D1F9C FOREIGN KEY (project_id) REFERENCES project (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE project ADD CONSTRAINT FK_2FB3D0EE873649CA FOREIGN KEY (managed_by_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE commit ADD CONSTRAINT FK_4ED42EAD166D1F9C FOREIGN KEY (project_id) REFERENCES project (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE commit ADD CONSTRAINT FK_4ED42EAD8DB60186 FOREIGN KEY (task_id) REFERENCES task (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE commit ADD CONSTRAINT FK_4ED42EADA76ED395 FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE commit ADD CONSTRAINT FK_4ED42EADDCD6CC49 FOREIGN KEY (branch_id) REFERENCES branch (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE level ADD CONSTRAINT FK_9AEACC13166D1F9C FOREIGN KEY (project_id) REFERENCES project (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('CREATE INDEX IDX_9AEACC13166D1F9C ON level (project_id)');
        $this->addSql('ALTER TABLE epic CHANGE project_id project_id INT NOT NULL');
        $this->addSql('ALTER TABLE epic ADD CONSTRAINT FK_19C95071166D1F9C FOREIGN KEY (project_id) REFERENCES project (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('CREATE INDEX IDX_19C95071166D1F9C ON epic (project_id)');
        $this->addSql('ALTER TABLE user DROP roles, CHANGE is_enabled is_enabled TINYINT(1) DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE task ADD part_id INT DEFAULT NULL, ADD assigned_to_id INT DEFAULT NULL, ADD assigned_by_id INT DEFAULT NULL, DROP project_id, DROP assigned_to, DROP assigned_by, CHANGE status status VARCHAR(255) NOT NULL');
        $this->addSql('ALTER TABLE task ADD CONSTRAINT FK_527EDB254CE34BEC FOREIGN KEY (part_id) REFERENCES part (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE task ADD CONSTRAINT FK_527EDB255FB14BA7 FOREIGN KEY (level_id) REFERENCES level (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE task ADD CONSTRAINT FK_527EDB256B71E00E FOREIGN KEY (epic_id) REFERENCES epic (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE task ADD CONSTRAINT FK_527EDB256E6F1246 FOREIGN KEY (assigned_by_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('ALTER TABLE task ADD CONSTRAINT FK_527EDB25F4BD7827 FOREIGN KEY (assigned_to_id) REFERENCES user (id) ON UPDATE NO ACTION ON DELETE NO ACTION');
        $this->addSql('CREATE INDEX IDX_527EDB256E6F1246 ON task (assigned_by_id)');
        $this->addSql('CREATE INDEX IDX_527EDB25F4BD7827 ON task (assigned_to_id)');
        $this->addSql('CREATE INDEX IDX_527EDB255FB14BA7 ON task (level_id)');
        $this->addSql('CREATE INDEX IDX_527EDB254CE34BEC ON task (part_id)');
        $this->addSql('CREATE INDEX IDX_527EDB256B71E00E ON task (epic_id)');
    }
}
