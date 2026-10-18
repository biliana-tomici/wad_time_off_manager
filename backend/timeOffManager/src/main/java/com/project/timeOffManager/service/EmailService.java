package com.project.timeOffManager.service;

import com.project.timeOffManager.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public record SentEmail(
            String to,
            String subject,
            String body,
            String resetToken,
            String temporaryPassword,
            Instant sentAt
    ) {
    }

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;
    private final String frontendUrl;
    private final boolean smtpEnabled;
    private final CopyOnWriteArrayList<SentEmail> sentEmails = new CopyOnWriteArrayList<>();

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${app.mail.from:no-reply@timeoff.com}") String fromAddress,
                        @Value("${app.mail.frontend-url:http://localhost:4200}") String frontendUrl,
                        @Value("${app.mail.enabled:false}") boolean smtpEnabled) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        this.smtpEnabled = smtpEnabled;
    }

    public String buildResetLink(String token) {
        return frontendUrl + "/reset-password?token=" + token;
    }

    public void sendWelcomeEmail(User user, String initialPassword, String setupToken) {
        String link = buildResetLink(setupToken);
        String subject = "Welcome to Time Off Manager — Account Setup";
        String body = String.format(
                "Hello %s %s,\n\n" +
                "Your Time Off Manager account has been created.\n" +
                "Login Email: %s\n" +
                "Initial Password: %s\n\n" +
                "You can sign in directly with this password or set your own password within 24 hours using this one-time link:\n%s\n",
                user.getFirstName(), user.getLastName(), user.getEmail(), initialPassword, link
        );
        dispatch(user.getEmail(), subject, body, setupToken, initialPassword);
    }

    public void sendAdminPasswordResetEmail(User user, String temporaryPassword, String resetToken) {
        String link = buildResetLink(resetToken);
        String subject = "Time Off Manager — Your Password Has Been Reset";
        String body = String.format(
                "Hello %s %s,\n\n" +
                "An administrator has reset your Time Off Manager password.\n" +
                "Temporary Password: %s\n\n" +
                "You can sign in using this temporary password or choose a new password within 24 hours via this one-time link:\n%s\n",
                user.getFirstName(), user.getLastName(), temporaryPassword, link
        );
        dispatch(user.getEmail(), subject, body, resetToken, temporaryPassword);
    }

    public void sendForgotPasswordEmail(User user, String resetToken) {
        String link = buildResetLink(resetToken);
        String subject = "Time Off Manager — Password Reset Request";
        String body = String.format(
                "Hello %s %s,\n\n" +
                "We received a request to reset the password for your Time Off Manager account (%s).\n" +
                "Use the following one-time link within 24 hours to set a new password:\n%s\n\n" +
                "If you did not request this, you can safely ignore this email.\n",
                user.getFirstName(), user.getLastName(), user.getEmail(), link
        );
        dispatch(user.getEmail(), subject, body, resetToken, null);
    }

    public void sendPasswordChangedEmail(User user, String generatedPassword) {
        String subject = "Time Off Manager — Security Notice: Password Changed";
        String extra = (generatedPassword != null && !generatedPassword.isBlank())
                ? "\nYour new generated password is: " + generatedPassword + "\n"
                : "";
        String body = String.format(
                "Hello %s %s,\n\n" +
                "The password for your Time Off Manager account (%s) was just changed.%s\n" +
                "If you did not make this change, please contact your administrator immediately.\n",
                user.getFirstName(), user.getLastName(), user.getEmail(), extra
        );
        dispatch(user.getEmail(), subject, body, null, generatedPassword);
    }

    private void dispatch(String to, String subject, String body, String resetToken, String temporaryPassword) {
        if (to == null || to.isBlank()) {
            return;
        }
        SentEmail record = new SentEmail(to, subject, body, resetToken, temporaryPassword, Instant.now());
        sentEmails.add(record);
        log.info("[MAIL SERVICE] To: {} | Subject: {}\n{}", to, subject, body);

        if (smtpEnabled) {
            JavaMailSender sender = mailSenderProvider.getIfAvailable();
            if (sender != null) {
                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromAddress);
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(body);
                    sender.send(message);
                } catch (MailException ex) {
                    log.warn("Failed to send SMTP email to {}: {}", to, ex.getMessage());
                }
            }
        }
    }

    public List<SentEmail> getSentEmails() {
        return List.copyOf(sentEmails);
    }

    public void clearSentEmails() {
        sentEmails.clear();
    }
}
