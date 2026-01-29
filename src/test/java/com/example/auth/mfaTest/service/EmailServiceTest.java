package com.example.auth.mfaTest.service;

import com.example.auth.mfa.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailService
 */
class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        mimeMessage = mock(MimeMessage.class);
        emailService = new EmailService(mailSender);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendMfaCode_sendsEmailSuccessfully_whenValidParameters() throws MessagingException {
        // Arrange
        String toEmail = "user@example.com";
        String code = "123456";
        String userName = "John Doe";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendMfaCode(toEmail, code, userName));

        // Verify
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendMfaCode_throwsMessagingException_whenEmailSendingFails() {
        // Arrange
        String toEmail = "user@example.com";
        String code = "123456";
        String userName = "John Doe";

        doThrow(new RuntimeException("Email server error"))
            .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        MessagingException exception = assertThrows(MessagingException.class, 
            () -> emailService.sendMfaCode(toEmail, code, userName));

        // Verify
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Échec de l'envoi de l'email MFA"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_sendsEmailSuccessfully_whenValidParameters() throws MessagingException {
        // Arrange
        String toEmail = "user@example.com";
        String subject = "Test Email";
        String content = "<h1>Test Content</h1>";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendEmail(toEmail, subject, content));

        // Verify
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_throwsMessagingException_whenEmailSendingFails() {
        // Arrange
        String toEmail = "user@example.com";
        String subject = "Test Email";
        String content = "<h1>Test Content</h1>";

        doThrow(new RuntimeException("Email server error"))
            .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        MessagingException exception = assertThrows(MessagingException.class,
            () -> emailService.sendEmail(toEmail, subject, content));

        // Verify
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Échec de l'envoi de l'email"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
