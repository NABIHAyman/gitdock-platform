package edu.ehei.gitdock.gitdockauth.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, HandlerMappingIntrospector introspector) throws Exception {

        // Constructeur de matcher MVC : Indispensable pour Spring Boot 3 + H2/Postgres
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);

        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        // 1. Autoriser les erreurs internes (pour éviter les 403 sur les exceptions)
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        // 2. ROUTES PUBLIQUES (Login, Register, Activation)
                        // On utilise mvc.pattern() pour être sûr que ça matche
                        .requestMatchers(mvc.pattern("/api/auth/authenticate")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/register")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/user-activation/**")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/password-reset/**")).permitAll()
                        .requestMatchers(mvc.pattern("/error")).permitAll()

                        // 3. ROUTES SÉCURISÉES
                        .requestMatchers(mvc.pattern("/api/auth/users/available-roles")).authenticated()
                        .requestMatchers(mvc.pattern("/api/auth/companies/**")).hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers(mvc.pattern("/api/auth/projects/**")).authenticated()

                        // Gestion des droits sur les utilisateurs
                        .requestMatchers(mvc.pattern("/api/auth/users/**")).hasAnyAuthority(
                                "ROLE_SUPER_ADMIN",
                                "ROLE_COMPANY_ADMIN",
                                "ROLE_WORKSPACE_OWNER", // Votre rôle ID 3 est bien ici !
                                "ROLE_MANAGER",
                                "ROLE_DEVELOPER"
                        )

                        // 4. TOUT LE RESTE EST BLOQUÉ
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Vos URLs frontend (Gateway et accès direct)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://localhost:8080"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}