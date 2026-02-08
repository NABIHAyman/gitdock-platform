package edu.ehei.gitdock.gitdockauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration de l'exécution asynchrone.
 * <p>
 * Cette classe active le support @Async de Spring et définit un pool de threads dédié.
 * Cela permet d'exécuter des tâches longues (comme l'envoi d'emails) dans des threads séparés,
 * libérant ainsi le thread principal pour répondre immédiatement à l'utilisateur.
 * </p>
 */
@Configuration
@EnableAsync // Active la détection des méthodes annotées @Async dans le projet
public class AsyncConfig {

    /**
     * Définit le pool de threads personnalisé pour les tâches asynchrones.
     * On le nomme "taskExecutor" pour pouvoir l'invoquer spécifiquement via @Async("taskExecutor").
     *
     * @return Un exécuteur configuré pour gérer la charge de travail en arrière-plan.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Nombre de threads toujours actifs (même sans tâche).
        // Ici, 2 threads sont prêts à envoyer des mails instantanément.
        executor.setCorePoolSize(2);

        // Nombre maximum de threads si la charge augmente brusquement.
        // Si plus de 2 mails arrivent en même temps, on peut monter jusqu'à 5 threads simultanés.
        executor.setMaxPoolSize(5);

        // Taille de la file d'attente.
        // Si les 5 threads sont occupés, jusqu'à 500 tâches (emails) peuvent attendre ici
        // avant d'être traitées. Au-delà, elles risquent d'être rejetées.
        executor.setQueueCapacity(500);

        // Préfixe pour identifier facilement ces threads dans les logs (ex: GitDock-Mail-1)
        executor.setThreadNamePrefix("GitDock-Mail-");

        executor.initialize();
        return executor;
    }
}