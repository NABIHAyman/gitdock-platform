<?php

namespace App\Security;

use App\Entity\User;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Symfony\Component\Security\Http\Authenticator\AbstractAuthenticator;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\UserBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\Passport;
use Symfony\Component\Security\Http\Authenticator\Passport\SelfValidatingPassport;

class GatewayHeaderAuthenticator extends AbstractAuthenticator
{
    public function supports(Request $request): ?bool
    {
        // Ne s'active que si la Gateway a bien fait son travail et injecté le header
        return $request->headers->has('X-User-Id');
    }

    public function authenticate(Request $request): Passport
    {
        $userId = $request->headers->get('X-User-Id');
        // On récupère l'email injecté par la Gateway (fallback par défaut si absent)
        $email = $request->headers->get('X-User-Email', 'system@gitdock.com');

        // On crée un passeport auto-validé (pas de vérification de mot de passe nécessaire ici)
        return new SelfValidatingPassport(
            new UserBadge($email, function () use ($userId, $email) {
                // Création "à la volée" de l'utilisateur pour le contexte de cette requête.
                // Cela évite un appel BDD et rend `$this->getUser()` disponible dans les controllers !
                $user = new User();
                $user->setExternalId((int) $userId);
                $user->setEmail($email);
                $user->setRoles(['ROLE_USER']);

                return $user;
            })
        );
    }

    public function onAuthenticationSuccess(Request $request, TokenInterface $token, string $firewallName): ?Response
    {
        // Succès : on laisse la requête continuer vers le TaskController
        return null;
    }

    public function onAuthenticationFailure(Request $request, AuthenticationException $exception): ?Response
    {
        return new JsonResponse([
            'success' => false,
            'message' => 'Accès refusé par la Gateway de Sécurité.'
        ], Response::HTTP_UNAUTHORIZED);
    }
}
