package edu.ehei.gitdock.gitdockauth.strategy;

import edu.ehei.gitdock.gitdockauth.enums.ProjectPlatform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthStrategyFactory {

    // Spring Boot va injecter automatiquement toutes les classes qui implémentent IOAuthStrategy
    private final List<IOAuthStrategy> strategies;

    public IOAuthStrategy getStrategy(ProjectPlatform platform) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(platform))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("❌ Aucune stratégie OAuth trouvée pour la plateforme : {}", platform);
                    return new IllegalArgumentException("Plateforme OAuth non supportée : " + platform);
                });
    }
}