package com.example.auth.mfaTest.service;

import com.example.auth.mfa.entity.MfaCodeJpaEntity;
import com.example.auth.mfa.repository.MfaCodeRepository;
import com.example.auth.mfa.service.EmailService;
import com.example.auth.mfa.service.MfaService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MfaService
 */
class MfaServiceTest {

    private MfaCodeRepository mfaCodeRepository;
    private EmailService emailService;
    private MfaService mfaService;

    @BeforeEach
    void setUp() {
        mfaCodeRepository = mock(MfaCodeRepository.class);
        emailService = mock(EmailService.class);
        mfaService = new MfaService(mfaCodeRepository, emailService);

        // Configure les valeurs par défaut
        ReflectionTestUtils.setField(mfaService, "mfaCodeExpirationMinutes", 10);
        ReflectionTestUtils.setField(mfaService, "mfaCodeLength", 6);
    }

    @Test
    void createAndSendMfaCode_createsCodeAndSendsEmail_whenValidParameters() throws MessagingException {
        // Arrange
        Integer userId = 1;
        String userEmail = "user@example.com";
        String userName = "John Doe";

        doNothing().when(mfaCodeRepository).deleteUnusedCodesByUserId(userId);
        when(mfaCodeRepository.save(any(MfaCodeJpaEntity.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendMfaCode(anyString(), anyString(), anyString());

        // Act
        String sessionToken = mfaService.createAndSendMfaCode(userId, userEmail, userName);

        // Assert
        assertNotNull(sessionToken);
        assertFalse(sessionToken.isEmpty());

        verify(mfaCodeRepository, times(1)).deleteUnusedCodesByUserId(userId);
        verify(mfaCodeRepository, times(1)).save(any(MfaCodeJpaEntity.class));
        verify(emailService, times(1)).sendMfaCode(eq(userEmail), anyString(), eq(userName));
    }

    @Test
    void createAndSendMfaCode_deletesCodeAndThrowsException_whenEmailSendingFails() throws MessagingException {
        // Arrange
        Integer userId = 1;
        String userEmail = "user@example.com";
        String userName = "John Doe";

        MfaCodeJpaEntity savedCode = new MfaCodeJpaEntity();
        savedCode.setId(1L);

        doNothing().when(mfaCodeRepository).deleteUnusedCodesByUserId(userId);
        when(mfaCodeRepository.save(any(MfaCodeJpaEntity.class))).thenReturn(savedCode);
        doThrow(new MessagingException("Email server error"))
            .when(emailService).sendMfaCode(anyString(), anyString(), anyString());

        // Act & Assert
        assertThrows(MessagingException.class, 
            () -> mfaService.createAndSendMfaCode(userId, userEmail, userName));

        verify(mfaCodeRepository, times(1)).delete(savedCode);
    }

    @Test
    void validateMfaCode_returnsUserId_whenCodeIsValid() {
        // Arrange
        String sessionToken = "test-session-token";
        String code = "123456";
        Integer userId = 1;

        MfaCodeJpaEntity mfaCode = new MfaCodeJpaEntity();
        mfaCode.setId(1L);
        mfaCode.setUserId(userId);
        mfaCode.setCode(code);
        mfaCode.setSessionToken(sessionToken);
        mfaCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        mfaCode.setUsed(false);

        when(mfaCodeRepository.findValidCode(eq(sessionToken), eq(code), any(LocalDateTime.class)))
            .thenReturn(Optional.of(mfaCode));
        when(mfaCodeRepository.save(any(MfaCodeJpaEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Integer result = mfaService.validateMfaCode(sessionToken, code);

        // Assert
        assertEquals(userId, result);
        assertTrue(mfaCode.isUsed());
        verify(mfaCodeRepository, times(1)).save(mfaCode);
    }

    @Test
    void validateMfaCode_returnsNull_whenCodeIsInvalid() {
        // Arrange
        String sessionToken = "test-session-token";
        String code = "123456";

        when(mfaCodeRepository.findValidCode(eq(sessionToken), eq(code), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // Act
        Integer result = mfaService.validateMfaCode(sessionToken, code);

        // Assert
        assertNull(result);
        verify(mfaCodeRepository, never()).save(any());
    }

    @Test
    void hasMfaCode_returnsTrue_whenCodeExists() {
        // Arrange
        String sessionToken = "test-session-token";
        MfaCodeJpaEntity mfaCode = new MfaCodeJpaEntity();

        when(mfaCodeRepository.findBySessionToken(sessionToken))
            .thenReturn(Optional.of(mfaCode));

        // Act
        boolean result = mfaService.hasMfaCode(sessionToken);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasMfaCode_returnsFalse_whenCodeDoesNotExist() {
        // Arrange
        String sessionToken = "test-session-token";

        when(mfaCodeRepository.findBySessionToken(sessionToken))
            .thenReturn(Optional.empty());

        // Act
        boolean result = mfaService.hasMfaCode(sessionToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void getMfaCodeBySessionToken_returnsMfaCode_whenExists() {
        // Arrange
        String sessionToken = "test-session-token";
        MfaCodeJpaEntity mfaCode = new MfaCodeJpaEntity();
        mfaCode.setSessionToken(sessionToken);

        when(mfaCodeRepository.findBySessionToken(sessionToken))
            .thenReturn(Optional.of(mfaCode));

        // Act
        MfaCodeJpaEntity result = mfaService.getMfaCodeBySessionToken(sessionToken);

        // Assert
        assertNotNull(result);
        assertEquals(sessionToken, result.getSessionToken());
    }

    @Test
    void getMfaCodeBySessionToken_returnsNull_whenDoesNotExist() {
        // Arrange
        String sessionToken = "test-session-token";

        when(mfaCodeRepository.findBySessionToken(sessionToken))
            .thenReturn(Optional.empty());

        // Act
        MfaCodeJpaEntity result = mfaService.getMfaCodeBySessionToken(sessionToken);

        // Assert
        assertNull(result);
    }

    @Test
    void cleanupExpiredCodes_deletesExpiredCodes_successfully() {
        // Arrange
        doNothing().when(mfaCodeRepository).deleteExpiredAndUsedCodes(any(LocalDateTime.class));

        // Act & Assert
        assertDoesNotThrow(() -> mfaService.cleanupExpiredCodes());

        verify(mfaCodeRepository, times(1)).deleteExpiredAndUsedCodes(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredCodes_handlesException_gracefully() {
        // Arrange
        doThrow(new RuntimeException("Database error"))
            .when(mfaCodeRepository).deleteExpiredAndUsedCodes(any(LocalDateTime.class));

        // Act & Assert - Ne devrait pas lancer d'exception
        assertDoesNotThrow(() -> mfaService.cleanupExpiredCodes());
    }
}
