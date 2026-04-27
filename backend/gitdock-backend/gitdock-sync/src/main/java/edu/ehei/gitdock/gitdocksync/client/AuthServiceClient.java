package edu.ehei.gitdock.gitdocksync.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gitdock-auth", path = "/api/auth/oauth")
public interface AuthServiceClient {
    @GetMapping("/internal/token")
    String getGithubToken(@RequestParam("userId") Long userId);
}