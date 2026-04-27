package edu.ehei.gitdock.gitdockauth.config;

import edu.ehei.gitdock.gitdockauth.model.Role;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.model.UserRole;
import edu.ehei.gitdock.gitdockauth.enums.RoleName;
import edu.ehei.gitdock.gitdockauth.repository.RoleRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialiseur de données exécuté automatiquement au démarrage de l'application.
 * <p>
 * Son rôle unique est de vérifier l'existence du compte Super Administrateur
 * et de le créer s'il est manquant. Cela garantit un accès système dès le premier déploiement.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Order(2) // S'exécute en deuxième position (après la création des tables par Hibernate/Flyway)
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Méthode principale déclenchée au lancement de Spring Boot.
     */
    @Override
    public void run(String... args) {
        String adminEmail = "admin@gitdock.com";

        // 1. Vérification de l'existence : on ne crée l'admin que s'il n'existe pas déjà.
        // On utilise findByEmailAndIsDeletedFalse pour ignorer un éventuel compte admin qui aurait été "soft deleted".
        if (userAccountRepository.findByEmailAndIsDeletedFalse(adminEmail).isEmpty()) {

            // 2. Récupération ou création du Rôle SUPER_ADMIN.
            // Si le rôle n'existe pas encore en base (ex: base vide), on le crée à la volée.
            Role adminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(RoleName.ROLE_SUPER_ADMIN);
                        return roleRepository.save(role);
                    });

            // 3. Construction de l'objet Utilisateur Admin.
            UserAccount admin = UserAccount.builder()
                    .username("admin")
                    .email(adminEmail)
                    // Sécurité : Toujours encoder le mot de passe avant de le stocker
                    .password(passwordEncoder.encode("Admin123!"))
                    .firstName("Admin")
                    .lastName("System")
                    .isEnabled(true)       // Compte actif par défaut
                    .accountLocked(false)  // Compte déverrouillé
                    .isDeleted(false)
                    .build();

            // 4. Création de la liaison User <-> Role (Entité de jointure)
            UserRole adminUserRole = UserRole.builder()
                    .user(admin)
                    .role(adminRole)
                    .build();

            // 5. Association du rôle à l'utilisateur
            // (Nécessaire pour que le CascadeType.ALL de JPA sauvegarde aussi la relation)
            admin.getUserRoles().add(adminUserRole);

            // 6. Sauvegarde en base de données
            userAccountRepository.save(admin);

            log.info("SUPER ADMIN créé avec succès : {} / Mot de passe : Admin123!", adminEmail);
        } else {
            log.info("Le compte Super Admin existe déjà. Initialisation ignorée.");
        }
    }
}