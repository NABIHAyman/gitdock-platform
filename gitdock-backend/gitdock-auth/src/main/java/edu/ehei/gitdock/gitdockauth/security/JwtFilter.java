package edu.ehei.gitdock.gitdockauth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // =========================================
        // 🔥 BYPASS ENDPOINTS PUBLICS
        // =========================================
        if (
                path.startsWith("/actuator") ||
                        path.startsWith("/api/auth/authenticate") ||
                        path.startsWith("/api/auth/register") ||
                        path.startsWith("/api/auth/user-activation") ||
                        path.startsWith("/api/auth/password-reset")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // =========================================
        // 🔐 JWT EXTRACTION
        // =========================================
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);

            // sécurité anti-bug frontend
            if (jwt.isBlank() ||
                    "null".equals(jwt) ||
                    "undefined".equals(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userEmail = jwtService.extractEmail(jwt);

            // =========================================
            // 🔐 AUTHENTIFICATION
            // =========================================
            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            log.error("JWT Error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}