package com.example.Bankomat.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private static final String DEFAULT_FROM = "noreply@bankomat.com";
    private static final String ADMIN_EMAIL = "admin@bank.com";

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(DEFAULT_FROM);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(DEFAULT_FROM);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true указывает на HTML

            mailSender.send(message);
            log.info("HTML email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}", to, e);
            throw new RuntimeException("HTML email sending failed", e);
        }
    }

    public void sendAdminNotification(String subject, String text) {
        sendEmail(ADMIN_EMAIL, subject, text);
    }
    public void sendAccountLockedEmail(String userEmail, String userName) {
        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: #d32f2f;">Ваш аккаунт временно заблокирован</h2>
                    <p>Уважаемый(ая) %s,</p>
                    <p>Ваш аккаунт был временно заблокирован после 3 неудачных попыток входа.</p>
                    <p>Если это были не вы, пожалуйста, свяжитесь с поддержкой.</p>
                    <hr>
                    <p style="color: #757575;">Это автоматическое сообщение, пожалуйста, не отвечайте на него.</p>
                </body>
            </html>
            """.formatted(userName);

        sendHtmlEmail(userEmail, "Блокировка аккаунта", htmlContent);
    }
}
