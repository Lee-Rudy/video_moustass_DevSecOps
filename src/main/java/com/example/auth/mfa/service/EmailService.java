package com.example.auth.mfa.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service pour l'envoi d'emails, notamment les codes MFA
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un code MFA par email
     * 
     * @param toEmail Email du destinataire
     * @param code Code MFA à 6 chiffres
     * @param userName Nom de l'utilisateur
     * @throws MessagingException Si l'envoi échoue
     */
    public void sendMfaCode(String toEmail, String code, String userName) throws MessagingException {
        logger.info("Envoi du code MFA à l'adresse : {}", toEmail);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Code de vérification MFA - Moustass Video");

        String htmlContent = buildMfaEmailHtml(code, userName);
        helper.setText(htmlContent, true);

        try {
            mailSender.send(message);
            logger.info("Code MFA envoyé avec succès à : {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du code MFA à {}: {}", toEmail, e.getMessage());
            throw new MessagingException("Échec de l'envoi de l'email MFA", e);
        }
    }

    /**
     * Construit le contenu HTML de l'email MFA
     */
    private String buildMfaEmailHtml(String code, String userName) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .greeting {
                        font-size: 16px;
                        color: #333;
                        margin-bottom: 20px;
                    }
                    .code-container {
                        background-color: #f8f9fa;
                        border: 2px dashed #667eea;
                        border-radius: 8px;
                        padding: 20px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .code {
                        font-size: 36px;
                        font-weight: bold;
                        color: #667eea;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .info {
                        font-size: 14px;
                        color: #666;
                        line-height: 1.6;
                        margin: 20px 0;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 12px 16px;
                        margin: 20px 0;
                        font-size: 13px;
                        color: #856404;
                    }
                    .footer {
                        background-color: #f8f9fa;
                        padding: 20px 30px;
                        text-align: center;
                        font-size: 12px;
                        color: #999;
                        border-top: 1px solid #e9ecef;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Moustass Video</h1>
                    </div>
                    <div class="content">
                        <p class="greeting">Bonjour <strong>%s</strong>,</p>
                        <p class="info">
                            Vous avez demande a vous connecter avec Google. Pour des raisons de securite, 
                            veuillez entrer le code de verification ci-dessous :
                        </p>
                        <div class="code-container">
                            <div class="code">%s</div>
                        </div>
                        <p class="info">
                            Ce code est valide pendant <strong>10 minutes</strong>. 
                            Ne partagez ce code avec personne.
                        </p>
                        <div class="warning">
                            Si vous n'avez pas demande ce code, veuillez ignorer cet email et 
                            securiser votre compte immediatement.
                        </div>
                    </div>
                    <div class="footer">
                        <p>Cet email a ete envoye automatiquement par Moustass Video.</p>
                        <p>&copy; 2026 Moustass Video. Tous droits reserves.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        
        return String.format(template, userName, code);
    }

    /**
     * Envoie un email de notification (pour usage futur)
     * 
     * @param toEmail Email du destinataire
     * @param subject Sujet de l'email
     * @param content Contenu de l'email
     * @throws MessagingException Si l'envoi échoue
     */
    public void sendEmail(String toEmail, String subject, String content) throws MessagingException {
        logger.info("Envoi d'email à : {}", toEmail);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content, true);

        try {
            mailSender.send(message);
            logger.info("Email envoyé avec succès à : {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage());
            throw new MessagingException("Échec de l'envoi de l'email", e);
        }
    }
}
