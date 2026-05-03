# Initial State - gitdock-gamification

## Contexte au demarrage

Le service etait dans un etat monolithique local, sans couche de profil de progression utilisateur dediee.
Le domaine exposait les entites de gamification principales (`Badge`, `Level`, `Tag`, `LevelTagRequirement`, `XpConfig`) mais ne contenait pas encore les entites cibles `UserGamificationProfile` et `UserTagProgress`.

## Etat technique observe

- `Data/ApplicationDbContext.cs` ne declarait pas de `DbSet` pour les profils utilisateur de gamification.
- Le dossier `Migrations/` contenait un historique important de migrations EF Core liees a l'ancien schema.
- Aucune entite `User.cs` n'etait presentee dans `Domain/` au moment de l'analyse.
- Le dossier `docs/` n'existait pas a la racine du service.

## Impact architectural

Le service n'etait pas encore aligne avec la cible "microservice event-driven" decrite dans les instructions.
La base devait etre preparee pour une regeneration propre des migrations apres isolation du domaine gamification.
