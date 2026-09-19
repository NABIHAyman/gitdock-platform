package edu.ehei.gitdock.gitdockauth.service.interfaces;
import edu.ehei.gitdock.gitdockauth.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;

public interface IOAuthService {
    String getAuthorizationUrl(ProjectPlatform platform);
    void exchangeCodeForToken(ProjectPlatform platform, String code, UserAccount user);
    void refreshAccessToken(ProjectPlatform platform, UserAccount user);
    String getAccessToken(ProjectPlatform platform, UserAccount user);
}