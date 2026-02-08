package edu.ehei.gitdock.gitdockdiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class GitdockDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitdockDiscoveryApplication.class, args);
    }

}
