# État Initial : GitDock Task (Monolithe)

Ce document décrit l'état de l'application `gitdock-task` avant le refactoring vers une architecture microservices Event-Driven.

## Architecture Actuelle (Monolithique)
Le service `gitdock-task` fonctionne de manière très couplée et possède dans sa base de données des entités qui ne devraient pas lui appartenir dans une architecture distribuée saine. On observe notamment le problème d'"Integration Hell" et de duplication de données.

### Entités Problématiques (Dupliquées)
Le dossier `src/Entity` (du backend) contient les entités suivantes qui appartiennent légitimement à d'autres services :
- `User.php` (Devrait appartenir uniquement à `gitdock-auth`)
- `Project.php` (Devrait appartenir uniquement à `gitdock-project`)
- `Branch.php` (Devrait appartenir uniquement à `gitdock-project`)
- `Commit.php` (Devrait appartenir uniquement à `gitdock-project`)

Ces entités ont été dupliquées pour satisfaire des relations Doctrine ORM (ex: `ManyToOne`, `OneToMany`) directement depuis l'entité `Task`.

### Impact sur le Code
- **Couplage Fort :** L'entité `Task.php` possède des relations directes par objet (et Doctrine) vers `User`, `Project`, etc.
- **Repositories Multiples :** Présence de `UserRepository.php`, `ProjectRepository.php`, `BranchRepository.php` et `CommitRepository.php` qui maintiennent cette duplication.
- **Données Incohérentes :** Toute mise à jour d'un utilisateur ou d'un projet dans son service d'origine nécessiterait une synchronisation manuelle lourde vers cette base locale.
- **Manque de Couche Inter-services :** Pas de communication synchrone (HTTP/REST via des Clients) ou asynchrone (RabbitMQ) structurée.

## Objectif du Refactoring
La transformation consiste à :
1. Purger ces entités et rompre leurs relations ORM.
2. Utiliser uniquement de simples identifiants (`$projectId`, `$assignedToUserId`).
3. Mettre en place des Clients HTTP pour valider ces IDs auprès des services de référence synchrones.
4. Mettre en place des Events Producers & Handlers via RabbitMQ pour réagir aux événements extérieurs et notifier les autres contextes.
