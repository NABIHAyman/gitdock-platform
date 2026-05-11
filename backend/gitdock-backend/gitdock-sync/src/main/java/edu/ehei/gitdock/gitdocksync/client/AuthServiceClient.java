package edu.ehei.gitdock.gitdocksync.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gitdock-auth", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @GetMapping("/api/auth/oauth/internal/token")
    String getGithubToken(@RequestParam("userId") Long userId);

    @GetMapping("/api/auth/verify")
    boolean verifyToken(@RequestParam("token") String token);
}