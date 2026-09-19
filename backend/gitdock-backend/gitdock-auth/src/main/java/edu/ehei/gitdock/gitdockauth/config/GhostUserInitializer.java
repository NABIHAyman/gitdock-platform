package edu.ehei.gitdock.gitdockauth.config;

import edu.ehei.gitdock.gitdockauth.model.Company;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.CompanyRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GhostUserInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String GHOST_EMAIL = "ghost@gitdock.system";

    @Override
    public void run(String... args) throws Exception {
        if (!userAccountRepository.existsByEmail(GHOST_EMAIL)) {

            Company systemCompany = companyRepository.findByName("System")
                    .orElseGet(() -> companyRepository.save(Company.builder()
                            .name("System")
                            .isPersonal(false)
                            .isDeleted(true) // Caché
                            .build()));

            UserAccount ghost = UserAccount.builder()
                    .firstName("Utilisateur")
                    .lastName("Supprimé")
                    .username("ghost_user")
                    .email(GHOST_EMAIL)
                    .password(passwordEncoder.encode("IMPOSSIBLE_TO_GUESS_PASSWORD_" + System.currentTimeMillis()))
                    .isEnabled(false)
                    // .accountLocked(true) // ⚠️ Décommente cette ligne SI le champ existe toujours dans ton UserAccount actuel
                    .isDeleted(true) // Caché par défaut des listes
                    .company(systemCompany)
                    .avatarUrl("https://ui-avatars.com/api/?name=Ghost+User&background=random")
                    .build();

            userAccountRepository.save(ghost);
            log.info("👻 Ghost User système initialisé avec succès !");
        } else {
            log.info("👻 Ghost User système déjà présent.");
        }
    }
}