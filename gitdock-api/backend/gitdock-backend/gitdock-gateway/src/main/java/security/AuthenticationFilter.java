package edu.ehei.gitdock.gitdockgateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private edu.ehei.gitdock.gitdockgateway.security.RouteValidator validator;

    @Autowired
    private edu.ehei.gitdock.gitdockgateway.security.JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // Si la route n'est pas dans la liste blanche...
            if (validator.isSecured.test(exchange.getRequest())) {

                // 1. Vérifie si le header Authorization est présent
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    logger.warn("Tentative d'accès sans Header Authorization");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                } else {
                    logger.warn("Header Authorization malformé");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                try {
                    // 2. Vérifie la validité du Token
                    if (jwtUtil.isInvalid(authHeader)) {
                        logger.warn("Token JWT invalide ou expiré");
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // 3. Extraction des données
                    Claims claims = jwtUtil.extractAllClaims(authHeader);
                    String userId = String.valueOf(claims.get("userId"));
                    String companyId = String.valueOf(claims.get("companyId"));
                    String email = claims.getSubject();
                    String roles = String.valueOf(claims.get("authorities"));

                    // 4. On modifie la requête pour y injecter les informations en clair !
                    exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-Company-Id", companyId != null ? companyId : "")
                            .header("X-User-Email", email)
                            .header("X-User-Roles", roles)
                            .build();

                } catch (Exception e) {
                    logger.error("Erreur d'authentification dans la Gateway : {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }

            // Tout est bon, on laisse passer au microservice cible !
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}