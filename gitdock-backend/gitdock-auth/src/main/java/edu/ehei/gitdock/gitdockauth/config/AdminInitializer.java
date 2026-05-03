package edu.ehei.gitdock.gitdockauth.config;

import edu.ehei.gitdock.gitdockauth.model.Role;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.model.UserRole;
import edu.ehei.gitdock.gitdockauth.enums.RoleName;
import edu.ehei.gitdock.gitdockauth.repository.RoleRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(2)
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserAccountRepository userAccountRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String adminEmail = "admin@gitdock.com";

        if (userAccountRepository.findByEmailAndIsDeletedFalse(adminEmail).isEmpty()) {

            Role adminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(RoleName.ROLE_SUPER_ADMIN);
                        return roleRepository.save(role);
                    });

            UserAccount admin = UserAccount.builder()
                    .username("admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin123!"))
                    .firstName("Admin")
                    .lastName("System")
                    .isEnabled(true)
                    .accountLocked(false)
                    .isDeleted(false)
                    .build();

            UserRole adminUserRole = UserRole.builder()
                    .user(admin)
                    .role(adminRole)
                    .build();

            admin.getUserRoles().add(adminUserRole);

            userAccountRepository.save(admin);

            log.info("SUPER ADMIN créé avec succès : " + adminEmail + " / Mot de passe : Admin123!");

        } else {
            log.info("Le compte Super Admin existe déjà. Initialisation ignorée.");
        }
    }
}