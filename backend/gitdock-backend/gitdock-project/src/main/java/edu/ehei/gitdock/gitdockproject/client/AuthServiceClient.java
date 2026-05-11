package edu.ehei.gitdock.gitdockproject.client;

import edu.ehei.gitdock.gitdockproject.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockproject.dto.UserSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Le "name" doit correspondre EXACTEMENT au nom enregistré dans Eureka par le service d'Auth.
@FeignClient(name = "gitdock-auth", fallback = AuthFallback.class)
public interface AuthServiceClient {

    // ✅ Le token est maintenant géré automatiquement par FeignClientInterceptor
    @GetMapping("/api/auth/users/summaries")
    List<UserSummaryDTO> getUsersSummaries(@RequestParam("ids") List<Long> ids);

    @GetMapping("/api/auth/users/by-email")
    UserSummaryDTO getUserByEmail(@RequestParam("email") String email);

    @PostMapping("/api/auth/users/internal/invite")
    UserSummaryDTO inviteUser(@RequestBody InviteCollaboratorRequestDTO request);

    @PostMapping("/api/auth/users/internal/by-emails")
    List<UserSummaryDTO> getUsersByEmailsInternal(@RequestBody List<String> emails);

    @GetMapping("/api/auth/oauth/internal/token")
    String getGithubToken(@RequestParam("userId") Long userId);

}