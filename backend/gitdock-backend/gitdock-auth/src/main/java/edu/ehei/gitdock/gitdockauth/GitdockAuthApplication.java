package edu.ehei.gitdock.gitdockauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GitdockAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitdockAuthApplication.class, args);
    }

}
