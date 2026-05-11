<<<<<<< HEAD
=======
/*
>>>>>>> f15b548226eb791a8b13956d26c47455fe8cc944
package edu.ehei.gitdock.gitdockgateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> f15b548226eb791a8b13956d26c47455fe8cc944

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        corsConfig.addAllowedMethod("*");

        corsConfig.addAllowedHeader("*");

        corsConfig.setAllowCredentials(true);

<<<<<<< HEAD
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

=======
        corsConfig.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Applique cette règle à TOUTES les routes qui passent par la Gateway
>>>>>>> f15b548226eb791a8b13956d26c47455fe8cc944
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
<<<<<<< HEAD
=======
*/
>>>>>>> f15b548226eb791a8b13956d26c47455fe8cc944
