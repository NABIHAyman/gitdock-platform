package edu.ehei.gitdock.gitdockauth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// import jakarta.validation.constraints.NotNull; // (Note: Pas strictement nécessaire ici, mais conservé pour ne pas modifier votre code)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull; // Préférez celui-ci pour Spring
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre de sécurité personnalisé exécuté une seule fois par requête.
 * Son rôle est d'intercepter les requêtes HTTP entrantes, d'extraire le token JWT
 * de l'en-tête "Authorization", de le valider et d'authentifier l'utilisateur
 * dans le contexte de sécurité Spring si tout est correct.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Logique principale du filtrage.
     *
     * @param request     La requête HTTP entrante.
     * @param response    La réponse HTTP sortante.
     * @param filterChain La chaîne de filtres suivante à exécuter.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Vérification de la présence et du format de l'en-tête Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Pas de token ? On passe la main au filtre suivant.
            // (L'accès sera refusé plus loin si la route est protégée).
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extraction du token (on enlève le préfixe "Bearer ")
            jwt = authHeader.substring(7);

            // Vérifications défensives conservées pour éviter les erreurs bizarres venant du frontend
            // (ex: string "null" ou "undefined" envoyée par erreur par le JS)
            if (jwt == null || jwt.trim().isEmpty() || "null".equals(jwt) || "undefined".equals(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Extraction de l'email (subject) depuis le token
            userEmail = jwtService.extractEmail(jwt);

            // 4. Validation et Authentification
            // Si on a un email et que l'utilisateur n'est pas encore authentifié dans le contexte actuel...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // a. On charge les détails de l'utilisateur depuis la base de données
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // b. On vérifie si le token est cryptographiquement valide et non expiré
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // c. Création de l'objet d'authentification Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Pas de credentials (mot de passe) nécessaire ici car déjà authentifié par token
                            userDetails.getAuthorities() // Les rôles/autorisations
                    );

                    // d. Ajout de détails supplémentaires (ex: IP, Session ID) à l'authentification
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // e. **Enregistrement final** de l'utilisateur dans le contexte de sécurité.
                    // C'est ici que l'utilisateur devient "connecté" pour le reste de la requête.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // En cas d'erreur (token malformé, expiré, signature invalide...), on loggue l'erreur
            // mais on ne bloque pas brutalement. On laisse le contexte de sécurité vide.
            // Spring Security rejettera l'accès (403 Forbidden) si la route demandée nécessite une auth.
            log.error("Erreur d'authentification JWT: {}", e.getMessage());
        }

        // 5. Passage au maillon suivant de la chaîne (ex: le contrôleur final)
        filterChain.doFilter(request, response);
    }
}