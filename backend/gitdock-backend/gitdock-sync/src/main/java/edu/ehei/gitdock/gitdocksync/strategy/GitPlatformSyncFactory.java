package edu.ehei.gitdock.gitdocksync.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitPlatformSyncFactory {

    // Spring injecte TOUTES les implémentations de IGitPlatformSyncStrategy
    private final List<IGitPlatformSyncStrategy> strategies;

    /**
     * Récupère la stratégie appropriée pour une plateforme.
     * @param platform Le nom de la plateforme (ex: "GITHUB")
     * @return La stratégie capable de gérer cette plateforme
     */
    public IGitPlatformSyncStrategy getStrategy(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new UnsupportedPlatformException("La plateforme ne peut pas être null ou vide");
        }

        IGitPlatformSyncStrategy strategy = strategies.stream()
                .filter(s -> s.supports(platform))
                .findFirst()
                .orElse(null);

        if (strategy == null) {
            String availablePlatforms = String.join(", ",
                    strategies.stream()
                            .map(IGitPlatformSyncStrategy::getPlatformName)
                            .toList()
            );

            log.warn("❌ Plateforme non supportée: {}. Plateformes disponibles: {}", platform, availablePlatforms);
            throw new UnsupportedPlatformException("Plateforme '" + platform + "' non supportée.");
        }

        log.info("✅ Stratégie sélectionnée pour {}: {}", platform, strategy.getClass().getSimpleName());
        return strategy;
    }

    /**
     * Exception custom pour une plateforme non supportée.
     */
    public static class UnsupportedPlatformException extends RuntimeException {
        public UnsupportedPlatformException(String message) {
            super(message);
        }
    }
}