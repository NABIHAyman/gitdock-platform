package edu.ehei.gitdock.gitdockproject.client;

import edu.ehei.gitdock.gitdockproject.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockproject.dto.UserSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Le "name" doit correspondre EXACTEMENT au nom enregistré dans Eureka par le service d'Auth.
// Le "path" est le préfixe commun à toutes les routes de l'Auth.
@FeignClient(name = "gitdock-auth", fallback = AuthFallback.class/*, path = "/api/auth/users"*/)
public interface AuthServiceClient {

    // ✅ Ajout de @RequestHeader pour forcer l'injection du JWT
    @GetMapping("/api/auth/users/summaries")
    List<UserSummaryDTO> getUsersSummaries(
            @RequestHeader("Authorization") String token,
            @RequestParam("ids") List<Long> ids
    );

    // ✅ Idem ici
    @GetMapping("/api/auth/users/by-email")
    UserSummaryDTO getUserByEmail(
            @RequestHeader("Authorization") String token,
            @RequestParam("email") String email
    );

    @PostMapping("/api/auth/users/internal/invite")
    UserSummaryDTO inviteUser(
            @RequestHeader("Authorization") String token,
            @RequestBody InviteCollaboratorRequestDTO request
    );

    @PostMapping("/api/auth/users/internal/by-emails")
    List<UserSummaryDTO> getUsersByEmailsInternal(@RequestBody List<String> emails);

    @GetMapping("/api/auth/oauth/internal/token")
    String getGithubToken(
            @RequestHeader("Authorization") String token,
            @RequestParam("userId") Long userId
    );

}