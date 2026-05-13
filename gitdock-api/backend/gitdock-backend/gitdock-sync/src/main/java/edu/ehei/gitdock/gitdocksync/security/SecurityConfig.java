package edu.ehei.gitdock.gitdocksync.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🟢 LAISSE PASSER LES WEBHOOKS DE GITHUB !
                        .requestMatchers("/api/sync/webhooks/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerUserFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}