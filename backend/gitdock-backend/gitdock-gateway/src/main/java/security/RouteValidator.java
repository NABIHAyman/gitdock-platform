package edu.ehei.gitdock.gitdockgateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    // La liste blanche : les routes qui n'ont PAS besoin de Token JWT
    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/authenticate",
            "/api/auth/user-activation",
            "/api/auth/request-password-reset",
            "/api/auth/reset-password",
            "/api/auth/oauth", // GitHub Login
            "/ws-notifications", // WebSockets (gérés autrement)
            "/api/ws/sentinel",  // WebSockets Sentinel
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}