package com.auth.backendauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendActivationEmail(String to, String token) {

        String activationLink= "http://localhost:5173/activate?token=" + token;

        String messageText =
                "Welcome to GITDOCK 🚀\n\n" +
                        "Hello,\n\n" +
                        "Thank you for creating your Gitdock account.\n\n" +
                        "To activate your account, please click the link below:\n" +
                        activationLink + "\n\n" +
                        "If you did not create this account, please ignore this email.\n\n" +
                        "—\n" +
                        "Gitdock Team\n" +
                        "Support: support@gitdock.io";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Activate your Gitdock account");
        message.setText(messageText);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        String messageText =
                "Gitdock Password Reset 🔐\n\n" +
                        "Hello,\n\n" +
                        "We received a request to reset your password.\n\n" +
                        "You can reset it using the link below:\n" +
                        resetLink + "\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "—\n" +
                        "Gitdock Security Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your Gitdock password");
        message.setText(messageText);

        mailSender.send(message);
    }
}
