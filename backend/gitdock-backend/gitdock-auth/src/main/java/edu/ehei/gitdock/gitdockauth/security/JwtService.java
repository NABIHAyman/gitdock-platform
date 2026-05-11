package edu.ehei.gitdock.gitdockauth.security;

import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service utilitaire responsable de la gestion des JSON Web Tokens (JWT).
 * <p>
 * Ses principales responsabilités sont :
 * 1. Générer des tokens (Access & Refresh) signés cryptographiquement.
 * 2. Extraire des informations (Claims) depuis un token existant (ex: email, expiration).
 * 3. Valider l'intégrité et la validité temporelle d'un token.
 * </p>
 */
@Service
public class JwtService {

    // Durée de validité du token d'accès (injectée depuis application.yml)
    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    // Durée de validité du refresh token (injectée depuis application.yml)
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    // Clé secrète utilisée pour signer le token (doit être complexe et gardée privée)
    @Value("${application.security.jwt.secret}")
    private String jwtSecret;

    /**
     * Extrait l'email (qui sert de username/subject) depuis le token JWT.
     *
     * @param token Le token JWT brut.
     * @return L'email contenu dans le "Subject" du token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Méthode générique pour extraire n'importe quelle donnée (Claim) du token.
     *
     * @param token          Le token JWT.
     * @param claimsResolver Une fonction qui définit quelle donnée récupérer depuis l'objet Claims.
     * @param <T>            Le type de la donnée retournée.
     * @return La donnée extraite.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un token d'accès pour un utilisateur donné.
     * Ajoute automatiquement l'ID de l'utilisateur dans les claims pour faciliter les requêtes ultérieures.
     *
     * @param userDetails Les détails de l'utilisateur authentifié.
     * @return La chaîne JWT signée.
     */
    public String generateToken(UserDetails userDetails){
        Map<String, Object> extraClaims = new HashMap<>();

        // Si l'objet UserDetails est bien notre entité UserAccount, on injecte son ID
        if (userDetails instanceof UserAccount user) {
            extraClaims.put("userId", user.getId());

            if (user.getCompany() != null) {
                extraClaims.put("companyId", user.getCompany().getId());
                extraClaims.put("company_plan", user.getCompany().getSubscriptionPlan().name());
            } else {
                extraClaims.put("company_plan", "FREE"); // Sécurité par défaut
            }

        }

        return generateToken(extraClaims, userDetails);
    }

    /**
     * Surcharge pour générer un token avec des claims personnalisés spécifiques.
     */
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return buildToken(claims, userDetails, jwtExpiration);
    }

    /**
     * Génère un Refresh Token (généralement durée de vie plus longue, sans claims superflus).
     *
     * @param userDetails L'utilisateur.
     * @return Le refresh token signé.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Méthode cœur qui construit le JWT.
     * Elle définit le Sujet, les Claims, les dates (émission/expiration) et signe le tout.
     *
     * @param extraClaims Données supplémentaires à inclure dans le payload.
     * @param userDetails L'utilisateur concerné.
     * @param expiration  La durée de validité en millisecondes.
     * @return Le token JWT final.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        // Transformation des autorités (Rôles) en liste de Strings simple pour le payload
        var authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder()
                .setClaims(extraClaims)                 // Ajout des données custom (ex: userId)
                .setSubject(userDetails.getUsername())  // Définition de l'email comme Subject
                .claim("authorities", authorities)      // Ajout des rôles
                .setIssuedAt(new Date(System.currentTimeMillis())) // Date de création
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Date d'expiration
                .signWith(getSignKey())                 // Signature cryptographique
                .compact();
    }

    /**
     * Vérifie si un token est valide pour un utilisateur donné.
     * Un token est valide si :
     * 1. L'email extrait du token correspond à l'email de l'utilisateur.
     * 2. La date d'expiration n'est pas passée.
     *
     * @param token       Le token à vérifier.
     * @param userDetails L'utilisateur censé être le propriétaire du token.
     * @return true si valide, false sinon.
     */
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Vérifie si la date d'expiration du token est passée.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parse le token pour récupérer l'ensemble du payload (Claims).
     * Cette méthode vérifiera implicitement la signature : si la signature est invalide,
     * une exception sera levée par la librairie JJWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Décrit la clé secrète (encodée en Base64 dans la config) en une clé cryptographique HMAC.
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}