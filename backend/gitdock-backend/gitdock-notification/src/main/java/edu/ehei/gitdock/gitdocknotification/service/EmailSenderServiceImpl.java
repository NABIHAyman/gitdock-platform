package edu.ehei.gitdock.gitdocknotification.service;

import edu.ehei.gitdock.gitdocknotification.service.interfaces.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

/**
 * Implémentation du service d'envoi d'emails.
 * Ce service utilise JavaMailSender pour l'envoi SMTP et Thymeleaf pour générer
 * le contenu HTML des emails à partir de templates.
 * Toutes les méthodes d'envoi sont asynchrones pour ne pas bloquer l'utilisateur.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    // Injection de l'adresse email de l'expéditeur depuis le fichier application.yml
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envoie un email d'activation de compte.
     * La méthode est annotée @Async pour s'exécuter dans un thread séparé.
     *
     * @param toEmail        L'adresse du destinataire.
     * @param firstName      Le prénom de l'utilisateur (pour personnaliser l'email).
     * @param lastName       Le nom de l'utilisateur.
     * @param activationLink Le lien complet (URL) sur lequel l'utilisateur doit cliquer.
     */
    @Override
    @Async("taskExecutor") // Utilisation explicite de notre Executor configuré dans AsyncConfig
    public void sendActivationEmail(String toEmail, String firstName, String lastName, String activationLink) {
        try {
            // 1. Préparation du contexte Thymeleaf (variables à injecter dans le HTML)
            Context context = new Context();
            context.setVariables(Map.of(
                    "FIRST_NAME", firstName != null ? firstName : "",
                    "LAST_NAME", lastName != null ? lastName : "",
                    "ACTIVATION_LINK", activationLink
            ));

            // 2. Génération du corps HTML à partir du template 'account-activation.html'
            String htmlContent = templateEngine.process("account-activation", context);

            // 3. Envoi effectif de l'email
            sendHtmlEmail(toEmail, "Activez votre compte GitDock", htmlContent);

            log.info("Email d'activation envoyé avec succès à {}", toEmail);

        } catch (Exception e) {
            // On catch l'exception pour éviter de faire planter le processus d'inscription
            // si le serveur mail ne répond pas.
            log.error("ERREUR CRITIQUE lors de l'envoi de l'email à {}", toEmail, e);
        }
    }

    /**
     * Envoie un email de réinitialisation de mot de passe.
     *
     * @param toEmail   L'adresse du destinataire.
     * @param firstName Prénom de l'utilisateur.
     * @param lastName  Nom de l'utilisateur.
     * @param resetLink Le lien contenant le token de reset.
     */
    @Override
    @Async("taskExecutor")
    public void sendPasswordResetEmail(String toEmail, String firstName, String lastName, String resetLink) {
        try {
            // 1. Configuration des variables pour le template
            Context context = new Context();
            context.setVariables(Map.of(
                    "FIRST_NAME", firstName != null ? firstName : "",
                    "LAST_NAME", lastName != null ? lastName : "",
                    "RESET_LINK", resetLink
            ));

            // 2. Traitement du template 'reset-password.html'
            String htmlContent = templateEngine.process("reset-password", context);

            // 3. Envoi de l'email
            sendHtmlEmail(toEmail, "Réinitialisation mot de passe GitDock", htmlContent);

            log.info("Email de reset envoyé à {}", toEmail);

        } catch (Exception e) {
            log.error("ERREUR lors de l'envoi de l'email de reset à {}", toEmail, e);
        }
    }

    /**
     * Envoie un email d'invitation à rejoindre un projet.
     *
     * @param to             Adresse email de l'invité.
     * @param firstName      Prénom de l'invité (peut être null si inconnu).
     * @param lastName       Nom de l'invité.
     * @param invitationLink Lien pour accepter l'invitation.
     * @param projectName    Nom du projet concerné.
     */
    @Override
    @Async("taskExecutor")
    public void sendInvitationEmail(String to, String firstName, String lastName, String invitationLink, String projectName) {
        try {
            // 1. Préparation du contexte avec le nom du projet
            Context context = new Context();
            context.setVariables(Map.of(
                    "FIRST_NAME", firstName != null ? firstName : "",
                    "LAST_NAME", lastName != null ? lastName : "",
                    "INVITATION_LINK", invitationLink,
                    "PROJECT_NAME", projectName
            ));

            // 2. Traitement du template 'invitation-activation.html'
            String htmlContent = templateEngine.process("invitation-activation", context);

            // 3. Envoi de l'email
            sendHtmlEmail(to, "Invitation à rejoindre " + projectName + " sur GitDock", htmlContent);

            log.info("Email d'invitation envoyé à {}", to);

        } catch (Exception e) {
            log.error("ERREUR lors de l'envoi de l'invitation à {}", to, e);
        }
    }

    /**
     * Méthode utilitaire privée pour construire et envoyer un email HTML via SMTP.
     *
     * @param toEmail   Destinataire.
     * @param subject   Sujet de l'email.
     * @param htmlBody  Contenu HTML généré par Thymeleaf.
     * @throws MessagingException Si la création du message MIME échoue.
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) throws MessagingException {
        // Création d'un message MIME (Multipurpose Internet Mail Extensions) supportant le HTML
        MimeMessage message = mailSender.createMimeMessage();

        // Utilisation du Helper pour simplifier la configuration (encodage UTF-8, mode multipart activé)
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // 'true' indique que le contenu est du HTML et non du texte brut

        // Envoi via le serveur SMTP configuré
        mailSender.send(message);
    }
}