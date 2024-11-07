package org.zakariafarih.syncly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmailVerificationEmail(String toEmail, String token) {
        String verificationLink = "https://frontendapp.com/verify-email?token=" + token;
        String subject = "Email Verification";
        String body = "Please click the link below to verify your email address:\n" + verificationLink;

        sendEmail(toEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = "https://frontendapp.com/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = "To reset your password, click the link below:\n" + resetLink;

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("email@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

