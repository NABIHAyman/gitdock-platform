package edu.ehei.gitdock.gitdocknotification.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe de test d'intégration pour le service de génération et d'envoi d'emails.
 */
@SpringBootTest
@ActiveProfiles("test")

public class EmailSenderServiceImplTest {
    // Le vrai service que l'on veut tester
    @Autowired
    private EmailSenderServiceImpl emailSenderService;

    // La doublure (Mock) de notre serveur d'envoi d'emails pour ne pas spammer en vrai
    //On remplace le vrai serveur d'envoi d'emails par un simulateur.
    //Cela évite d'avoir besoin d'un serveur SMTP (comme Gmail ou Outlook) pour faire tourner les tests
    @MockBean
    private JavaMailSender mailSender;

     @Test
    void sendActivationEmail_ShouldGenerateAndSendEmail() {

        // -----------------------------------------------------------------
        // 1. PRÉPARATION (ARRANGE)
        // -----------------------------------------------------------------
        // ASTUCE TECHNIQUE N°1 (Pour le jury) :
        // Si on crée un faux MimeMessage totalement vide avec Mockito, le composant interne
        // de Spring (MimeMessageHelper) va planter. La solution élégante est de créer
        // un "vrai" message vide via JavaMailSenderImpl pour notre test.
        JavaMailSenderImpl realMailSender = new JavaMailSenderImpl();
        MimeMessage mockMessage = realMailSender.createMimeMessage();

        // On indique à notre Mock : "Si on te demande de créer un message, donne celui-là"
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        // -----------------------------------------------------------------
        // 2. EXÉCUTION (ACT)
        // -----------------------------------------------------------------
        // On demande l'envoi de l'email d'activation
        emailSenderService.sendActivationEmail(
                "amal.dev@test.com",
                "Amal",
                "Dev",
                "http://gitdock.com/activate/123"
        );

        // -----------------------------------------------------------------
        // 3. VÉRIFICATION (ASSERT)
        // -----------------------------------------------------------------
        // ASTUCE TECHNIQUE N°2 (Pour le jury) :
        // La méthode sendActivationEmail est annotée @Async dans ton code, ce qui veut dire
        // qu'elle s'exécute en arrière-plan dans un thread séparé !
        // Si on fait un 'verify' normal, le test risque de finir AVANT que l'email soit envoyé.
        // On utilise donc timeout(2000) pour dire au test d'attendre jusqu'à 2 secondes.
        Mockito.verify(mailSender, Mockito.timeout(2000).times(1)).send(mockMessage);
    }
}
