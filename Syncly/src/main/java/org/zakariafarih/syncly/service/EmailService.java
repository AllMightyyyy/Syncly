package org.zakariafarih.syncly.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendPasswordResetEmail(String toEmail, String token) {
        // Implement email sending logic , i think ill be using javaEmailSender ?
        // Email should contain a link to frontend application where user can reset their password
        // e.g "http://localhost:3000/reset-password?token=" + token

        String resetLink = "https://syncly.netlify.app/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = "To reset your password, click the link below:\n" + resetLink;

        // TODO -> Choose where our app will be hosted and the email sending service we will be using

        // Placeholder for now
        System.out.println("Email sent to: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}
