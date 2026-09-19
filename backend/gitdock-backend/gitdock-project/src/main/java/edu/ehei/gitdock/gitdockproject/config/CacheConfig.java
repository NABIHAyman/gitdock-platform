package edu.ehei.gitdock.gitdockproject.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
@EnableCaching // 👈 C'est ce qui allume le moteur de cache Spring !
public class CacheConfig {

    public static final String CACHE_COMMITS = "project-commits";
    public static final String CACHE_BRANCHES = "project-branches";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheCustomizer() {
        return builder -> builder
                .withCacheConfiguration(CACHE_COMMITS,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5))) // Cache les commits pour 5 minutes
                .withCacheConfiguration(CACHE_BRANCHES,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(15))); // Cache les branches pour 15 minutes
    }
}