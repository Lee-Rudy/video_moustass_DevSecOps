package com.example.auth.auditTest.service;

import com.example.auth.audit.entity.AuditLogJpaEntity;
import com.example.auth.audit.repository.AuditLogRepository;
import com.example.auth.audit.service.AuditLogService;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    private AuditLogRepository auditLogRepository;
    private SpringDataUsersRepository userRepository;
    private AuditLogService auditLogService;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        auditLogRepository = mock(AuditLogRepository.class);
        userRepository = mock(SpringDataUsersRepository.class);
        auditLogService = new AuditLogService(auditLogRepository, userRepository);
        mockRequest = mock(HttpServletRequest.class);
    }

    @Test
    void logAction_shouldSaveAuditLog_withUserInfo() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        user.setMail("alice@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("192.168.1.100");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        auditLogService.logAction(1, "USER_LOGIN", "users", 1, "Login successful", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals(1, savedLog.getActorUserId());
        assertEquals("Alice", savedLog.getActorName());
        assertEquals("alice@test.com", savedLog.getActorMail());
        assertEquals("USER_LOGIN", savedLog.getAction());
        assertEquals("users", savedLog.getEntity());
        assertEquals(1, savedLog.getEntityId());
        assertEquals("Login successful", savedLog.getMessage());
        assertEquals("192.168.1.100", savedLog.getIpAddress());
        assertEquals("Mozilla/5.0", savedLog.getUserAgent());
        assertNotNull(savedLog.getCreatedAt());
    }

    @Test
    void logAction_shouldHandleUnknownUser() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("TestAgent");

        auditLogService.logAction(999, "UNKNOWN_ACTION", "entity", 1, "Test message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals(999, savedLog.getActorUserId());
        assertNull(savedLog.getActorName());
        assertNull(savedLog.getActorMail());
        assertEquals("UNKNOWN_ACTION", savedLog.getAction());
    }

    @Test
    void logAction_shouldHandleNullUserAgent() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Bob");
        user.setMail("bob@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn(null);

        auditLogService.logAction(1, "TEST_ACTION", "test", 1, "Test", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNull(savedLog.getUserAgent());
        assertEquals("10.0.0.1", savedLog.getIpAddress());
    }

    @Test
    void logAction_shouldSaveWithAllProvidedData() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName("TestUser");
        user.setMail("test@example.com");

        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("192.168.100.50");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Custom User Agent");

        auditLogService.logAction(5, "ORDER_CREATED", "orders", 123, "Order created successfully", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals(5, savedLog.getActorUserId());
        assertEquals("TestUser", savedLog.getActorName());
        assertEquals("test@example.com", savedLog.getActorMail());
        assertEquals("ORDER_CREATED", savedLog.getAction());
        assertEquals("orders", savedLog.getEntity());
        assertEquals(123, savedLog.getEntityId());
        assertEquals("Order created successfully", savedLog.getMessage());
        assertEquals("192.168.100.50", savedLog.getIpAddress());
        assertEquals("Custom User Agent", savedLog.getUserAgent());
    }

    @Test
    void logAction_shouldCallRepositoryOnce() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);

        verify(auditLogRepository, times(1)).save(any(AuditLogJpaEntity.class));
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void logAction_shouldSetCreatedAtToCurrentTime() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        long beforeCall = System.currentTimeMillis();
        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);
        long afterCall = System.currentTimeMillis();

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNotNull(savedLog.getCreatedAt());
        
        // Vérifier que createdAt est proche du moment actuel (dans une marge de quelques secondes)
        long createdAtMillis = savedLog.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertTrue(createdAtMillis >= beforeCall - 1000);
        assertTrue(createdAtMillis <= afterCall + 1000);
    }

    @Test
    void logAction_shouldExtractIpFromXForwardedFor() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals("203.0.113.195", savedLog.getIpAddress());
    }

    @Test
    void logAction_shouldExtractIpFromXRealIp_whenXForwardedForMissing() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("X-Real-IP")).thenReturn("192.168.1.50");
        when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals("192.168.1.50", savedLog.getIpAddress());
    }

    @Test
    void logAction_shouldExtractIpFromRemoteAddr_whenHeadersMissing() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(mockRequest.getRemoteAddr()).thenReturn("172.16.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals("172.16.0.1", savedLog.getIpAddress());
    }

    @Test
    void logAction_shouldTruncateUserAgent_whenTooLong() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        String longUserAgent = "A".repeat(300); // 300 characters

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn(longUserAgent);

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNotNull(savedLog.getUserAgent());
        assertEquals(255, savedLog.getUserAgent().length());
    }

    @Test
    void logAction_shouldHandleNullRequest() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", null);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNull(savedLog.getIpAddress());
        assertNull(savedLog.getUserAgent());
    }

    @Test
    void logAction_shouldHandleNullUserId() {
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(null, "ACTION", "entity", 1, "message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNull(savedLog.getActorUserId());
        assertNull(savedLog.getActorName());
        assertNull(savedLog.getActorMail());
    }

    @Test
    void logActionWithMetadata_shouldSaveMetadataAsJson() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        metadata.put("key3", true);

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", metadata, mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNotNull(savedLog.getMetadata());
        assertTrue(savedLog.getMetadata().contains("key1"));
        assertTrue(savedLog.getMetadata().contains("value1"));
        assertTrue(savedLog.getMetadata().contains("key2"));
        assertTrue(savedLog.getMetadata().contains("123"));
        assertTrue(savedLog.getMetadata().contains("key3"));
        assertTrue(savedLog.getMetadata().contains("true"));
    }

    @Test
    void logActionWithMetadata_shouldHandleNullMetadata() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", null, mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNull(savedLog.getMetadata());
    }

    @Test
    void logActionWithMetadata_shouldHandleEmptyMetadata() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", java.util.Collections.emptyMap(), mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNull(savedLog.getMetadata());
    }

    @Test
    void logActionWithMetadata_shouldEscapeSpecialCharacters() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("quote", "He said \"Hello\"");
        metadata.put("backslash", "Path\\to\\file");
        metadata.put("newline", "Line1\nLine2");
        metadata.put("tab", "Col1\tCol2");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", metadata, mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNotNull(savedLog.getMetadata());
        assertTrue(savedLog.getMetadata().contains("\\\""));
        assertTrue(savedLog.getMetadata().contains("\\\\"));
        assertTrue(savedLog.getMetadata().contains("\\n"));
        assertTrue(savedLog.getMetadata().contains("\\t"));
    }

    @Test
    void logActionWithMetadata_shouldHandleNullValues() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("nullKey", null);
        metadata.put("validKey", "value");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", metadata, mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNotNull(savedLog.getMetadata());
        assertTrue(savedLog.getMetadata().contains("null"));
        assertTrue(savedLog.getMetadata().contains("validKey"));
    }

    @Test
    void logAction_shouldHandleEmptyXForwardedFor() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("");
        when(mockRequest.getHeader("X-Real-IP")).thenReturn("192.168.1.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals("192.168.1.1", savedLog.getIpAddress());
    }

    @Test
    void logAction_shouldHandleEmptyXRealIp() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("X-Real-IP")).thenReturn("");
        when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertEquals("10.0.0.1", savedLog.getIpAddress());
    }

    @Test
    void logActionWithMetadata_shouldHandleComplexObjects() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("User");
        user.setMail("user@test.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Agent");

        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("object", new Object());
        metadata.put("double", 3.14);

        auditLogService.logAction(1, "ACTION", "entity", 1, "message", metadata, mockRequest);

        ArgumentCaptor<AuditLogJpaEntity> captor = ArgumentCaptor.forClass(AuditLogJpaEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogJpaEntity savedLog = captor.getValue();
        assertNotNull(savedLog.getMetadata());
    }
}
