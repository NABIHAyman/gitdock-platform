package edu.ehei.gitdock.gitdocksync.client;

import org.springframework.stereotype.Component;

@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public String getGithubToken(Long userId) {
        System.err.println("⚠️ [FALLBACK] Impossible de récupérer le token GitHub pour l'utilisateur : " + userId);
        return null;
    }

    @Override
    public boolean verifyToken(String token) {
        System.err.println("⚠️ [FALLBACK] Impossible de contacter gitdock-auth pour la vérification.");
        return false;
    }
}