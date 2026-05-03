package edu.ehei.gitdock.gitdockgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GitdockGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitdockGatewayApplication.class, args);
    }

}
