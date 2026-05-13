package edu.ehei.gitdock.gitdockgateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // On utilise la MÊME clé secrète que gitdock-auth !
    @Value("${application.security.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isInvalid(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // Si le token est malformé ou expiré, il est invalide
        }
    }
}