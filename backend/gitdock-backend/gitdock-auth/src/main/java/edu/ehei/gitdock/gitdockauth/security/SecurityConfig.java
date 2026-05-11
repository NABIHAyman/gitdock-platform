package edu.ehei.gitdock.gitdockauth.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, HandlerMappingIntrospector introspector) throws Exception {

        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);

        httpSecurity
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        // 1. SWAGGER & API DOCS
                        .requestMatchers(mvc.pattern("/v3/api-docs/**")).permitAll()
                        .requestMatchers(mvc.pattern("/swagger-ui/**")).permitAll()
                        .requestMatchers(mvc.pattern("/swagger-ui.html")).permitAll()
                        .requestMatchers(mvc.pattern("/swagger-resources/**")).permitAll()
                        .requestMatchers(mvc.pattern("/webjars/**")).permitAll()

                        // 2. ERREURS ET DISPATCHERS
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        // 3. ROUTES PUBLIQUES
                        .requestMatchers(mvc.pattern("/api/auth/authenticate")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/register")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/user-activation/**")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/password-reset/**")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/users/summaries")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/users/by-email")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/oauth/internal/token")).permitAll()
                        .requestMatchers(mvc.pattern("/api/auth/users/internal/**")).permitAll()
                        .requestMatchers(mvc.pattern("/error")).permitAll()

                        // 4. ROUTES SÉCURISÉES
                        .requestMatchers(mvc.pattern("/api/auth/companies/**")).hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers(mvc.pattern("/api/auth/users/**")).hasAnyAuthority(
                                "ROLE_SUPER_ADMIN", "ROLE_COMPANY_ADMIN", "ROLE_WORKSPACE_OWNER", "ROLE_MANAGER", "ROLE_DEVELOPER"
                        )

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}