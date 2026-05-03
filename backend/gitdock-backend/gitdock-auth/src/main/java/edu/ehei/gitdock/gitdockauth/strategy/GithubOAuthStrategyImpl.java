package edu.ehei.gitdock.gitdockauth.strategy.impl;

package edu.ehei.gitdock.gitdockauth.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import edu.ehei.gitdock.gitdockauth.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockauth.model.AccessToken;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.AccessTokenRepository;
import edu.ehei.gitdock.gitdockauth.strategy.IOAuthStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubOAuthStrategyImpl implements IOAuthStrategy {

    private final AccessTokenRepository accessTokenRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String githubRedirectUri;

    @Override
    public boolean supports(ProjectPlatform platform) {
        return platform == ProjectPlatform.GITHUB;
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + githubClientId +
                "&redirect_uri=" + githubRedirectUri +
                "&scope=repo,user:email";
    }

    @Override
    public void exchangeCodeForToken(String code, UserAccount user) {
        String tokenUrl = "https://github.com/login/oauth/access_token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("code", code);
        body.add("redirect_uri", githubRedirectUri);

        try {
            JsonNode json = restClient.post()
                    .uri(tokenUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (json != null && json.has("error")) {
                throw new RuntimeException("Erreur GitHub: " + json.path("error_description").asText());
            }

            String accessTokenStr = json.path("access_token").asText();
            String refreshTokenStr = json.path("refresh_token").asText(null);
            long expiresIn = json.path("expires_in").asLong(0);
            LocalDateTime expiresAt = expiresIn > 0 ? LocalDateTime.now().plusSeconds(expiresIn) : null;

            GitHubProfile profile = fetchGitHubProfile(accessTokenStr);
            saveToken(user, ProjectPlatform.GITHUB, accessTokenStr, refreshTokenStr, expiresAt, profile);

        } catch (Exception e) {
            log.error("Erreur OAuth GitHub", e);
            throw new RuntimeException("Échec de l'authentification GitHub : " + e.getMessage());
        }
    }

    @Override
    public void refreshAccessToken(UserAccount user) {
        AccessToken existingToken = accessTokenRepository.findByUserAndPlatform(user, ProjectPlatform.GITHUB)
                .orElseThrow(() -> new RuntimeException("Aucun token trouvé pour cet utilisateur."));

        if (existingToken.getRefreshToken() == null || existingToken.getRefreshToken().isBlank()) {
            throw new RuntimeException("Pas de refresh token disponible. Veuillez vous reconnecter à GitHub.");
        }

        String tokenUrl = "https://github.com/login/oauth/access_token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", existingToken.getRefreshToken());

        try {
            JsonNode json = restClient.post()
                    .uri(tokenUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (json != null && json.has("error")) {
                throw new RuntimeException("Le refresh token a expiré ou est invalide.");
            }

            String newAccessToken = json.path("access_token").asText();
            String newRefreshToken = json.path("refresh_token").asText(null);
            long expiresIn = json.path("expires_in").asLong(0);

            existingToken.setAccessToken(newAccessToken);
            if (newRefreshToken != null && !newRefreshToken.isBlank()) {
                existingToken.setRefreshToken(newRefreshToken);
            }
            existingToken.setExpiresAt(expiresIn > 0 ? LocalDateTime.now().plusSeconds(expiresIn) : null);

            accessTokenRepository.save(existingToken);
            log.info("Token GitHub rafraîchi avec succès pour {}", user.getEmail());

        } catch (Exception e) {
            throw new RuntimeException("Impossible de rafraîchir la connexion GitHub.");
        }
    }

    private GitHubProfile fetchGitHubProfile(String token) {
        JsonNode body = restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(JsonNode.class);

        return new GitHubProfile(
                body.path("login").asText(),
                body.path("avatar_url").asText(),
                body.path("id").asText()
        );
    }

    private void saveToken(UserAccount user, ProjectPlatform platform, String token, String refreshToken, LocalDateTime expiresAt, GitHubProfile profile) {
        AccessToken accessToken = accessTokenRepository.findByUserAndPlatform(user, platform)
                .orElse(AccessToken.builder().user(user).platform(platform).username(profile.username()).build());

        accessToken.setAccessToken(token);
        accessToken.setRefreshToken(refreshToken);
        accessToken.setExpiresAt(expiresAt);
        accessToken.setRemoteUsername(profile.username());
        accessToken.setRemoteAvatarUrl(profile.avatarUrl());
        accessToken.setRemoteId(profile.remoteId());

        accessTokenRepository.save(accessToken);
    }

    private record GitHubProfile(String username, String avatarUrl, String remoteId) {}
}