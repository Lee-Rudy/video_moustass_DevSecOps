package com.example.auth.auditTest.entity;

import com.example.auth.audit.entity.AuditLogJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogJpaEntityTest {

    @Test
    void shouldCreateEmptyEntity() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        assertNotNull(entity);
    }

    @Test
    void shouldSetAndGetId() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setId(100L);
        assertEquals(100L, entity.getId());
    }

    @Test
    void shouldSetAndGetActorUserId() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setActorUserId(5);
        assertEquals(5, entity.getActorUserId());
    }

    @Test
    void shouldSetAndGetActorName() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setActorName("Alice");
        assertEquals("Alice", entity.getActorName());
    }

    @Test
    void shouldSetAndGetActorMail() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setActorMail("alice@test.com");
        assertEquals("alice@test.com", entity.getActorMail());
    }

    @Test
    void shouldSetAndGetAction() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setAction("USER_LOGIN");
        assertEquals("USER_LOGIN", entity.getAction());
    }

    @Test
    void shouldSetAndGetEntity() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setEntity("users");
        assertEquals("users", entity.getEntity());
    }

    @Test
    void shouldSetAndGetEntityId() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setEntityId(123);
        assertEquals(123, entity.getEntityId());
    }

    @Test
    void shouldSetAndGetMessage() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        String message = "User logged in successfully";
        entity.setMessage(message);
        assertEquals(message, entity.getMessage());
    }

    @Test
    void shouldSetAndGetIpAddress() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setIpAddress("192.168.1.100");
        assertEquals("192.168.1.100", entity.getIpAddress());
    }

    @Test
    void shouldSetAndGetUserAgent() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        String userAgent = "Mozilla/5.0";
        entity.setUserAgent(userAgent);
        assertEquals(userAgent, entity.getUserAgent());
    }

    @Test
    void shouldSetAndGetMetadata() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        String metadata = "{\"key\":\"value\"}";
        entity.setMetadata(metadata);
        assertEquals(metadata, entity.getMetadata());
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void shouldSetAllFieldsCorrectly() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
        entity.setId(1L);
        entity.setActorUserId(10);
        entity.setActorName("Bob");
        entity.setActorMail("bob@test.com");
        entity.setAction("ORDER_CREATED");
        entity.setEntity("orders");
        entity.setEntityId(999);
        entity.setMessage("Order created successfully");
        entity.setIpAddress("10.0.0.1");
        entity.setUserAgent("TestAgent");
        entity.setMetadata("{}");
        entity.setCreatedAt(timestamp);

        assertEquals(1L, entity.getId());
        assertEquals(10, entity.getActorUserId());
        assertEquals("Bob", entity.getActorName());
        assertEquals("bob@test.com", entity.getActorMail());
        assertEquals("ORDER_CREATED", entity.getAction());
        assertEquals("orders", entity.getEntity());
        assertEquals(999, entity.getEntityId());
        assertEquals("Order created successfully", entity.getMessage());
        assertEquals("10.0.0.1", entity.getIpAddress());
        assertEquals("TestAgent", entity.getUserAgent());
        assertEquals("{}", entity.getMetadata());
        assertEquals(timestamp, entity.getCreatedAt());
    }

    @Test
    void shouldHandleNullValues() {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        
        entity.setActorName(null);
        entity.setActorMail(null);
        entity.setMessage(null);
        entity.setIpAddress(null);
        entity.setUserAgent(null);
        entity.setMetadata(null);
        entity.setCreatedAt(null);

        assertNull(entity.getActorName());
        assertNull(entity.getActorMail());
        assertNull(entity.getMessage());
        assertNull(entity.getIpAddress());
        assertNull(entity.getUserAgent());
        assertNull(entity.getMetadata());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void shouldCreateLoginLogEntry() {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setId(1L);
        log.setActorUserId(1);
        log.setActorName("Alice");
        log.setActorMail("alice@test.com");
        log.setAction("USER_LOGIN");
        log.setEntity("users");
        log.setEntityId(1);
        log.setMessage("Login successful");
        log.setIpAddress("192.168.1.1");
        log.setUserAgent("Chrome");
        log.setCreatedAt(LocalDateTime.now());

        assertEquals("USER_LOGIN", log.getAction());
        assertEquals("Alice", log.getActorName());
        assertTrue(log.getMessage().contains("Login"));
    }

    @Test
    void shouldCreateOrderLogEntry() {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setAction("ORDER_CREATED");
        log.setEntity("orders");
        log.setEntityId(456);
        log.setMessage("New order created");

        assertEquals("ORDER_CREATED", log.getAction());
        assertEquals("orders", log.getEntity());
        assertEquals(456, log.getEntityId());
    }

    @Test
    void shouldUpdateExistingLog() {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        log.setId(1L);
        log.setMessage("Original message");

        // Update
        log.setMessage("Updated message");
        log.setMetadata("new metadata");

        assertEquals("Updated message", log.getMessage());
        assertEquals("new metadata", log.getMetadata());
        assertEquals(1L, log.getId()); // ID should not change
    }

    @Test
    void shouldHandleDifferentActionTypes() {
        String[] actions = {"USER_LOGIN", "USER_LOGOUT", "USER_CREATED", "USER_DELETED", 
                           "ORDER_CREATED", "ORDER_VALIDATED", "ORDER_SIGNED"};
        
        for (String action : actions) {
            AuditLogJpaEntity log = new AuditLogJpaEntity();
            log.setAction(action);
            assertEquals(action, log.getAction());
        }
    }

    @Test
    void shouldHandleIPv6Address() {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        log.setIpAddress(ipv6);
        assertEquals(ipv6, log.getIpAddress());
    }

    @Test
    void shouldHandleLongUserAgent() {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        String longUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        log.setUserAgent(longUserAgent);
        assertEquals(longUserAgent, log.getUserAgent());
    }

    @Test
    void shouldHandleComplexMetadata() {
        AuditLogJpaEntity log = new AuditLogJpaEntity();
        String complexMetadata = "{\"order_id\":123,\"items\":[{\"id\":1,\"qty\":2},{\"id\":2,\"qty\":1}],\"total\":99.99}";
        log.setMetadata(complexMetadata);
        assertEquals(complexMetadata, log.getMetadata());
    }
}
