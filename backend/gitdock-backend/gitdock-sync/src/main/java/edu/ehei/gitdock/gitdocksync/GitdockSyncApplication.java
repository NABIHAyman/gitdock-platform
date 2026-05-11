package edu.ehei.gitdock.gitdocksync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // 👈 CETTE LIGNE MANQUAIT

@SpringBootApplication
@EnableFeignClients
@EnableJpaRepositories(basePackages = "edu.ehei.gitdock.gitdocksync.repository")
@EntityScan(basePackages = "edu.ehei.gitdock.gitdocksync.model")
public class GitdockSyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitdockSyncApplication.class, args);
    }
}