package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.exception.UserNotFoundException;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service central pour l'authentification Spring Security.
 *
 * Cette classe implémente l'interface standard {@link UserDetailsService}.
 * Son rôle unique est de charger les données d'un utilisateur (mot de passe, rôles, état)
 * à partir de la base de données lorsqu'une tentative de connexion survient.
 * Spring Security utilisera l'objet {@link UserDetails} retourné pour vérifier le mot de passe.
 *
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    /**
     * Charge un utilisateur par son identifiant (ici, l'email).
     *
     * Cette méthode est appelée automatiquement par le {@code AuthenticationManager}
     * lors du processus de login.
     *
     * @param email L'email fourni dans le formulaire de connexion (agit comme username).
     * @return L'objet UserDetails (notre entité UserAccount) s'il existe.
     * @throws UsernameNotFoundException (ou UserNotFoundException) si aucun utilisateur actif n'est trouvé.
     */
    @Override
    @Transactional // Important : Maintient la transaction ouverte pour charger les collections Lazy (ex: rôles)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // On cherche l'utilisateur par email en s'assurant qu'il n'est pas "supprimé" (Soft Delete).
        // Si l'utilisateur est marqué isDeleted = true, il ne pourra pas se connecter.
        return userAccountRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}