package edu.ehei.gitdock.gitdocknotification.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final HeaderUserFilter headerUserFilter;

    public SecurityConfig(HeaderUserFilter headerUserFilter) {
        this.headerUserFilter = headerUserFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // On désactive le CSRF car on est en mode API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws-notifications/**").permitAll() // On laisse passer les WebSockets
                        .requestMatchers("/actuator/**").permitAll()        // Monitoring
                        .anyRequest().authenticated()                        // Tout le reste nécessite le Header X-User-Id
                )
                // On injecte notre filtre AVANT le filtre d'authentification standard
                .addFilterBefore(headerUserFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}