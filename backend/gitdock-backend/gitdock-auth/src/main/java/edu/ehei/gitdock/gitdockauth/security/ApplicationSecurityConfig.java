package edu.ehei.gitdock.gitdockauth.security;

import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration des composants fondamentaux de la sécurité.
 * <p>
 * Cette classe enregistre dans le contexte Spring les beans nécessaires à l'authentification :
 * 1. UserDetailsService : Pour charger les utilisateurs depuis la BDD.
 * 2. PasswordEncoder : Pour le hachage des mots de passe.
 * 3. AuthenticationProvider : Pour lier les deux précédents et effectuer la vérification.
 * 4. AuthenticationManager : Pour déclencher l'authentification manuellement (ex: lors du login).
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationSecurityConfig {

    private final UserAccountRepository repository;

    /**
     * Définit comment Spring Security doit récupérer les informations d'un utilisateur.
     * On utilise ici une expression lambda qui se connecte directement à notre Repository JPA.
     *
     * @return Une implémentation de l'interface fonctionnelle UserDetailsService.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Configure le fournisseur d'authentification (DAO).
     * C'est le composant qui contient la logique réelle de vérification des identifiants.
     * Il compare le mot de passe fourni (haché à la volée) avec celui stocké en base.
     *
     * @return L'instance configurée de DaoAuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // On injecte notre service de récupération d'utilisateur
        authProvider.setUserDetailsService(userDetailsService());
        // On injecte notre encodeur de mot de passe
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expose le gestionnaire d'authentification principal.
     * Ce bean est utilisé dans le contrôleur ou le service d'authentification pour appeler
     * la méthode `authenticate()`.
     *
     * @param config La configuration automatique de Spring Security.
     * @return L'AuthenticationManager prêt à l'emploi.
     * @throws Exception Si la configuration échoue.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Définit l'algorithme de hachage des mots de passe.
     * BCrypt est l'algorithme standard recommandé, sécurisé et lent (pour contrer le brute-force).
     *
     * @return Une instance de BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}