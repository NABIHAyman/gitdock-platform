package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockauth.model.AccessToken;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.AccessTokenRepository;
import edu.ehei.gitdock.gitdockauth.service.interfaces.IOAuthService;
import edu.ehei.gitdock.gitdockauth.strategy.IOAuthStrategy;
import edu.ehei.gitdock.gitdockauth.strategy.OAuthStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements IOAuthService {

    private final OAuthStrategyFactory strategyFactory;
    private final AccessTokenRepository accessTokenRepository;

    @Override
    public String getAuthorizationUrl(ProjectPlatform platform) {
        IOAuthStrategy strategy = strategyFactory.getStrategy(platform);
        return strategy.getAuthorizationUrl();
    }

    @Override
    @Transactional
    public void exchangeCodeForToken(ProjectPlatform platform, String code, UserAccount user) {
        IOAuthStrategy strategy = strategyFactory.getStrategy(platform);
        strategy.exchangeCodeForToken(code, user);
    }

    @Override
    @Transactional
    public void refreshAccessToken(ProjectPlatform platform, UserAccount user) {
        IOAuthStrategy strategy = strategyFactory.getStrategy(platform);
        strategy.refreshAccessToken(user);
    }

    @Override
    public String getAccessToken(ProjectPlatform platform, UserAccount user) {
        // Cette méthode reste ici car elle tape directement en base (commune à toutes les plateformes)
        return accessTokenRepository.findByUserAndPlatform(user, platform)
                .map(AccessToken::getAccessToken)
                .orElse(null);
    }
}