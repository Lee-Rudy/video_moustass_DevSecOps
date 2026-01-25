package com.example.auth.loginTest.models;

import com.example.auth.login.entity.SignatureTransactionJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SignatureTransactionJpaEntityTest {

     @Test
    void prePersist_setsCreatedAtAndSignedAt_whenNull() {
        SignatureTransactionJpaEntity e = new SignatureTransactionJpaEntity();

        assertNull(e.getCreatedAt());
        assertNull(e.getSignedAt());

        e.prePersist();

        assertNotNull(e.getCreatedAt());
        assertNotNull(e.getSignedAt());
    }

    @Test
    void prePersist_doesNotOverrideExistingTimestamps() {
        SignatureTransactionJpaEntity e = new SignatureTransactionJpaEntity();
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime signed = LocalDateTime.of(2026, 1, 2, 10, 0);

        e.setCreatedAt(created);
        e.setSignedAt(signed);

        e.prePersist();

        assertEquals(created, e.getCreatedAt());
        assertEquals(signed, e.getSignedAt());
    }

    @Test
    void gettersAndSetters_work() {
        SignatureTransactionJpaEntity e = new SignatureTransactionJpaEntity();

        e.setId(10);
        e.setUserId(1);
        e.setTransactionSendTo("bob@gmail.com");
        e.setVideoName("video.mp4");
        e.setMontantTransaction(new BigDecimal("123.45"));
        e.setVideoHash("hash");
        e.setPathVideo("/tmp/video.mp4");
        e.setExpiredVideo(LocalDateTime.of(2026, 2, 1, 0, 0));
        e.setActive(false);
        e.setPublicKey("pub");
        e.setSignature("sig");
        e.setSignedAt(LocalDateTime.of(2026, 1, 3, 0, 0));
        e.setCreatedAt(LocalDateTime.of(2026, 1, 4, 0, 0));

        assertEquals(10, e.getId());
        assertEquals(1, e.getUserId());
        assertEquals("bob@gmail.com", e.getTransactionSendTo());
        assertEquals("video.mp4", e.getVideoName());
        assertEquals(new BigDecimal("123.45"), e.getMontantTransaction());
        assertEquals("hash", e.getVideoHash());
        assertEquals("/tmp/video.mp4", e.getPathVideo());
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0), e.getExpiredVideo());
        assertFalse(e.isActive());
        assertEquals("pub", e.getPublicKey());
        assertEquals("sig", e.getSignature());
        assertEquals(LocalDateTime.of(2026, 1, 3, 0, 0), e.getSignedAt());
        assertEquals(LocalDateTime.of(2026, 1, 4, 0, 0), e.getCreatedAt());
    }
    
}
