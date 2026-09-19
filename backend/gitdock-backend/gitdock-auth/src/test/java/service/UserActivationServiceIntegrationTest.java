package edu.ehei.gitdock.gitdockauth.service;

import edu.ehei.gitdock.gitdockauth.dto.SetPasswordRequestDTO;
import edu.ehei.gitdock.gitdockauth.model.ActivationToken;
import edu.ehei.gitdock.gitdockauth.model.UserAccount;
import edu.ehei.gitdock.gitdockauth.repository.ActivationTokenRepository;
import edu.ehei.gitdock.gitdockauth.repository.InvitationTokenRepository;
import edu.ehei.gitdock.gitdockauth.repository.UserAccountRepository;
import edu.ehei.gitdock.gitdockauth.service.interfaces.EmailSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class UserActivationServiceIntegrationTest {

    // POURQUOI : On teste le VRAI service d'activation
    @Autowired
    private UserActivationServiceImpl userActivationService;

    // POURQUOI : On simule toutes ses dépendances pour contrôler le scénario
    @MockBean
    private ActivationTokenRepository activationTokenRepository;

    @MockBean
    private UserAccountRepository userAccountRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailSenderService emailService;

    @MockBean
    private InvitationTokenRepository invitationTokenRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        // Nettoyage des compteurs de Mockito avant chaque test
        Mockito.reset(activationTokenRepository, userAccountRepository, passwordEncoder);
    }

    // =========================================================================
    // TEST 1 : ACTIVATION REUSSIE (Happy Path)
    // =========================================================================
    @Test
    void testActivateUser_Success() {
        // 1. ARRANGE : Préparation des données simulées
        SetPasswordRequestDTO request = new SetPasswordRequestDTO();
        request.setToken("mon-token-secret-123");
        request.setPassword("NouveauPass123!");
        request.setConfirmPassword("NouveauPass123!");

        // On crée un utilisateur "inactif" (isEnabled = false)
        UserAccount mockUser = new UserAccount();
        mockUser.setId(1L);
        mockUser.setEmail("amal@gitdock.com");
        mockUser.setEnabled(false); // 👈 L'état initial qu'on veut tester

        // On crée un token valide lié à cet utilisateur
        ActivationToken mockToken = new ActivationToken();
        mockToken.setToken("mon-token-secret-123");
        mockToken.setUsed(false);
        mockToken.setUser(mockUser);

        // COMMENT la donnée circule : On dit à Mockito "Si le service cherche ce token, donne-lui mockToken"
        Mockito.when(activationTokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(
                        any(String.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(mockToken));

        // On simule l'encodage du mot de passe
        Mockito.when(passwordEncoder.encode(request.getPassword())).thenReturn("mot_de_passe_hache");

        // 2. ACT : Déclenchement de l'activation
        assertDoesNotThrow(() -> {
            userActivationService.activateUser(request);
        });

        // 3. ASSERT : Vérification des actions effectuées par le service

        // A. Vérifier que le mot de passe a bien été encodé et mis à jour
        assertEquals("mot_de_passe_hache", mockUser.getPassword());

        // B. Vérifier que le token a bien été "brûlé" (utilisé)
        assertTrue(mockToken.isUsed());

        // C. LA VÉRIFICATION LA PLUS IMPORTANTE : L'utilisateur a-t-il été activé et sauvegardé ?
        // On utilise un ArgumentCaptor : c'est un "piège" qui capture l'objet exact qui a été envoyé à la base de données
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository, times(1)).save(userCaptor.capture());

        UserAccount savedUser = userCaptor.getValue();
        // On s'assure que le champ isEnabled est bien passé à TRUE
        assertTrue(savedUser.isEnabled(), "L'utilisateur devrait être activé !");
    }

    // =========================================================================
    // TEST 2 : ECHEC - Mots de passe différents
    // =========================================================================
    @Test
    void testActivateUser_Fail_PasswordMismatch() {
        // 1. ARRANGE
        SetPasswordRequestDTO request = new SetPasswordRequestDTO();
        request.setToken("mon-token-secret-123");
        request.setPassword("NouveauPass123!");
        request.setConfirmPassword("PAS-LE-MEME-MOT-DE-PASSE"); //  Différent !

        // 2. ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userActivationService.activateUser(request);
        });

        // Vérification du message d'erreur
        assertEquals("Les mots de passe ne correspondent pas", exception.getMessage());

        // On s'assure que RIEN n'a été sauvegardé en base de données
        verify(activationTokenRepository, times(0)).save(any(ActivationToken.class));
        verify(userAccountRepository, times(0)).save(any(UserAccount.class));
    }
}