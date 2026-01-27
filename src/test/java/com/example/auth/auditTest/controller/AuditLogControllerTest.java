package com.example.auth.auditTest.controller;

import com.example.auth.audit.controller.AuditLogController;
import com.example.auth.audit.entity.AuditLogJpaEntity;
import com.example.auth.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuditLogController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AuditLogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuditLogRepository auditLogRepository;

    @Test
    void getAllLogs_shouldReturnAllLogsSortedByDate() throws Exception {
        AuditLogJpaEntity log1 = createLog(1L, 1, "Alice", "alice@test.com", "USER_LOGIN", "users", 1, "Login successful");
        AuditLogJpaEntity log2 = createLog(2L, 2, "Bob", "bob@test.com", "USER_CREATED", "users", 2, "User created");
        AuditLogJpaEntity log3 = createLog(3L, 1, "Alice", "alice@test.com", "ORDER_CREATED", "orders", 10, "Order created");

        List<AuditLogJpaEntity> logs = Arrays.asList(log1, log2, log3);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(logs);

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].actorName").value("Alice"))
                .andExpect(jsonPath("$[0].actorMail").value("alice@test.com"))
                .andExpect(jsonPath("$[0].action").value("USER_LOGIN"))
                .andExpect(jsonPath("$[0].entity").value("users"))
                .andExpect(jsonPath("$[0].entityId").value(1))
                .andExpect(jsonPath("$[0].message").value("Login successful"))
                .andExpect(jsonPath("$[1].actorName").value("Bob"))
                .andExpect(jsonPath("$[1].action").value("USER_CREATED"))
                .andExpect(jsonPath("$[2].actorName").value("Alice"))
                .andExpect(jsonPath("$[2].action").value("ORDER_CREATED"));

        verify(auditLogRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllLogs_shouldReturnEmptyList_whenNoLogs() throws Exception {
        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(auditLogRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllLogs_shouldIncludeAllFields() throws Exception {
        AuditLogJpaEntity log = createLogWithAllFields();

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].actorUserId").value(5))
                .andExpect(jsonPath("$[0].actorName").value("TestUser"))
                .andExpect(jsonPath("$[0].actorMail").value("test@example.com"))
                .andExpect(jsonPath("$[0].action").value("TEST_ACTION"))
                .andExpect(jsonPath("$[0].entity").value("test_entity"))
                .andExpect(jsonPath("$[0].entityId").value(99))
                .andExpect(jsonPath("$[0].message").value("Test message"))
                .andExpect(jsonPath("$[0].ipAddress").value("192.168.1.1"))
                .andExpect(jsonPath("$[0].userAgent").value("TestAgent"))
                .andExpect(jsonPath("$[0].metadata").value("test metadata"));

        verify(auditLogRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllLogs_shouldHandleNullFields() throws Exception {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setId(50L);
        log.setActorUserId(1);
        log.setAction("ACTION");
        log.setEntity("entity");
        log.setEntityId(1);
        // Tous les autres champs sont null
        log.setActorName(null);
        log.setActorMail(null);
        log.setMessage(null);
        log.setIpAddress(null);
        log.setUserAgent(null);
        log.setMetadata(null);
        log.setCreatedAt(null);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].actorName").isEmpty())
                .andExpect(jsonPath("$[0].actorMail").isEmpty())
                .andExpect(jsonPath("$[0].message").isEmpty())
                .andExpect(jsonPath("$[0].createdAt").isEmpty());

        verify(auditLogRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllLogs_shouldConvertCreatedAtToString() throws Exception {
        AuditLogJpaEntity log = createLog(1L, 1, "User", "user@test.com", "ACTION", "entity", 1, "message");
        LocalDateTime now = LocalDateTime.now();
        log.setCreatedAt(now);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createdAt").isString());

        verify(auditLogRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllLogs_shouldReturnMultipleLogs_inCorrectOrder() throws Exception {
        AuditLogJpaEntity log1 = createLog(1L, 1, "Alice", "alice@test.com", "LOGIN", "users", 1, "msg1");
        AuditLogJpaEntity log2 = createLog(2L, 2, "Bob", "bob@test.com", "LOGOUT", "users", 2, "msg2");
        AuditLogJpaEntity log3 = createLog(3L, 3, "Charlie", "charlie@test.com", "UPDATE", "users", 3, "msg3");
        AuditLogJpaEntity log4 = createLog(4L, 4, "Dave", "dave@test.com", "DELETE", "users", 4, "msg4");

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(log1, log2, log3, log4));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].actorName").value("Alice"))
                .andExpect(jsonPath("$[1].actorName").value("Bob"))
                .andExpect(jsonPath("$[2].actorName").value("Charlie"))
                .andExpect(jsonPath("$[3].actorName").value("Dave"));

        verify(auditLogRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllLogs_shouldCallRepositoryOnce() throws Exception {
        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk());

        verify(auditLogRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllLogs_shouldReturnJsonContentType() throws Exception {
        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getAllLogs_shouldIncludeIpAndUserAgent() throws Exception {
        AuditLogJpaEntity log = createLog(1L, 1, "User", "user@test.com", "ACTION", "entity", 1, "message");
        log.setIpAddress("203.0.113.195");
        log.setUserAgent("Mozilla/5.0 Chrome");

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.singletonList(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ipAddress").value("203.0.113.195"))
                .andExpect(jsonPath("$[0].userAgent").value("Mozilla/5.0 Chrome"));
    }

    @Test
    void getAllLogs_shouldHandleLargeNumberOfLogs() throws Exception {
        List<AuditLogJpaEntity> logs = new java.util.ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            logs.add(createLog((long) i, i, "User" + i, "user" + i + "@test.com", 
                              "ACTION", "entity", i, "message " + i));
        }

        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(logs);

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[99].id").value(100));
    }

    @Test
    void getAllLogs_shouldMapEntityIdCorrectly() throws Exception {
        AuditLogJpaEntity log = createLog(1L, 1, "User", "user@test.com", "ACTION", "orders", 999, "Order created");

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.singletonList(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityId").value(999));
    }

    @Test
    void getAllLogs_shouldHandleNullMetadata() throws Exception {
        AuditLogJpaEntity log = createLog(1L, 1, "User", "user@test.com", "ACTION", "entity", 1, "message");
        log.setMetadata(null);

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.singletonList(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].metadata").isEmpty());
    }

    @Test
    void getAllLogs_shouldIncludeMetadataWhenPresent() throws Exception {
        AuditLogJpaEntity log = createLog(1L, 1, "User", "user@test.com", "ACTION", "entity", 1, "message");
        log.setMetadata("{\"key\":\"value\",\"number\":123}");

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.singletonList(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].metadata").value("{\"key\":\"value\",\"number\":123}"));
    }

    // Helper methods
    private AuditLogJpaEntity createLog(Long id, Integer actorUserId, String actorName, String actorMail,
                                        String action, String entity, Integer entityId, String message) {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setId(id);
        log.setActorUserId(actorUserId);
        log.setActorName(actorName);
        log.setActorMail(actorMail);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setMessage(message);
        log.setIpAddress("127.0.0.1");
        log.setUserAgent("TestAgent");
        log.setMetadata("{}");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private AuditLogJpaEntity createLogWithAllFields() {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setId(100L);
        log.setActorUserId(5);
        log.setActorName("TestUser");
        log.setActorMail("test@example.com");
        log.setAction("TEST_ACTION");
        log.setEntity("test_entity");
        log.setEntityId(99);
        log.setMessage("Test message");
        log.setIpAddress("192.168.1.1");
        log.setUserAgent("TestAgent");
        log.setMetadata("test metadata");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
