package edu.ehei.gitdock.gitdockproject.client;

import edu.ehei.gitdock.gitdockproject.dto.InviteCollaboratorRequestDTO;
import edu.ehei.gitdock.gitdockproject.dto.UserSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuthFallback implements AuthServiceClient {

    @Override
    public List<UserSummaryDTO> getUsersSummaries(String token, List<Long> ids) {
        log.warn("⚠️ [FEIGN FALLBACK] gitdock-auth injoignable. Retour de faux utilisateurs.");
        return ids.stream().map(id -> UserSummaryDTO.builder()
                .id(id)
                .firstName("Service")
                .lastName("Indisponible")
                .email("offline@gitdock.system")
                .isEnabled(false)
                .build()).collect(Collectors.toList());
    }

    @Override
    public UserSummaryDTO getUserByEmail(String token, String email) {
        log.warn("⚠️ [FEIGN FALLBACK] Impossible de joindre gitdock-auth pour {}", email);
        throw new RuntimeException("Service Auth indisponible");
    }

    @Override
    public UserSummaryDTO inviteUser(String token, InviteCollaboratorRequestDTO request) {
        throw new RuntimeException("Service Auth indisponible");
    }

    @Override
    public List<UserSummaryDTO> getUsersByEmailsInternal(List<String> emails) {
        return List.of();
    }

    @Override
    public String getGithubToken(String token, Long userId) {
        log.warn("⚠️ [FEIGN FALLBACK] Impossible de récupérer le token pour l'user {}", userId);
        return null;
    }

}