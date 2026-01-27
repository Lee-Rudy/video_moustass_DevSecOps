package com.example.auth.configTest;

import com.example.auth.config.JwtHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtHelperTest {

    private static final String VALID_SECRET = "this-is-a-very-long-secret-key-at-least-32-bytes-long-for-hs256";

    @Test
    void constructor_shouldCreateJwtHelper_withValidSecret() {
        assertDoesNotThrow(() -> new JwtHelper(VALID_SECRET));
    }

    @Test
    void constructor_shouldThrowException_whenSecretTooShort() {
        String shortSecret = "short-secret";
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> new JwtHelper(shortSecret));
        
        assertTrue(ex.getMessage().contains("au moins 32 octets"));
    }

    @Test
    void constructor_shouldThrowException_whenSecretIs31Bytes() {
        String secret31 = "1234567890123456789012345678901"; // 31 bytes
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> new JwtHelper(secret31));
        
        assertTrue(ex.getMessage().contains("au moins 32 octets"));
    }

    @Test
    void createToken_shouldGenerateValidToken() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token = jwtHelper.createToken(123);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void createToken_shouldIncludeUserId() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token = jwtHelper.createToken(42);
        
        // Parse the token manually to verify userId
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertEquals(42, claims.get("userId", Integer.class));
    }

    @Test
    void createToken_shouldSetSubjectToUserId() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token = jwtHelper.createToken(999);
        
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertEquals("999", claims.getSubject());
    }

    @Test
    void parseUserId_shouldExtractUserId_fromValidToken() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String token = jwtHelper.createToken(100);
        
        Integer userId = jwtHelper.parseUserId(token);
        
        assertEquals(100, userId);
    }

    @Test
    void parseUserId_shouldExtractCorrectUserId_forDifferentUsers() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token1 = jwtHelper.createToken(1);
        String token2 = jwtHelper.createToken(2);
        String token3 = jwtHelper.createToken(1000);
        
        assertEquals(1, jwtHelper.parseUserId(token1));
        assertEquals(2, jwtHelper.parseUserId(token2));
        assertEquals(1000, jwtHelper.parseUserId(token3));
    }

    @Test
    void parseUserId_shouldThrowException_whenTokenInvalid() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String invalidToken = "invalid.jwt.token";
        
        assertThrows(Exception.class, () -> jwtHelper.parseUserId(invalidToken));
    }

    @Test
    void parseUserId_shouldThrowException_whenTokenSignatureInvalid() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String token = jwtHelper.createToken(5);
        
        // Create another JwtHelper with different secret
        String differentSecret = "different-secret-key-at-least-32-bytes-long-for-testing-purposes";
        JwtHelper differentJwtHelper = new JwtHelper(differentSecret);
        
        // Should throw because signature doesn't match
        assertThrows(Exception.class, () -> differentJwtHelper.parseUserId(token));
    }

    @Test
    void parseUserId_shouldThrowException_whenTokenIsEmpty() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        assertThrows(Exception.class, () -> jwtHelper.parseUserId(""));
    }

    @Test
    void parseUserId_shouldThrowException_whenTokenIsNull() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        assertThrows(Exception.class, () -> jwtHelper.parseUserId(null));
    }

    @Test
    void createToken_shouldGenerateDifferentTokens_forDifferentUserIds() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token1 = jwtHelper.createToken(1);
        String token2 = jwtHelper.createToken(2);
        
        assertNotEquals(token1, token2);
    }

    @Test
    void createToken_shouldGenerateSameToken_forSameUserId() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token1 = jwtHelper.createToken(5);
        String token2 = jwtHelper.createToken(5);
        
        // Les tokens devraient être identiques car il n'y a pas d'expiration ni de champs aléatoires
        assertEquals(token1, token2);
    }

    @Test
    void createToken_shouldWorkWithNullUserId() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        // Devrait gérer null sans crash
        assertDoesNotThrow(() -> jwtHelper.createToken(null));
    }

    @Test
    void createToken_shouldWorkWithNegativeUserId() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token = jwtHelper.createToken(-1);
        
        assertNotNull(token);
        assertEquals(-1, jwtHelper.parseUserId(token));
    }

    @Test
    void createToken_shouldWorkWithZeroUserId() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token = jwtHelper.createToken(0);
        
        assertNotNull(token);
        assertEquals(0, jwtHelper.parseUserId(token));
    }

    @Test
    void createToken_shouldWorkWithLargeUserId() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String token = jwtHelper.createToken(Integer.MAX_VALUE);
        
        assertNotNull(token);
        assertEquals(Integer.MAX_VALUE, jwtHelper.parseUserId(token));
    }

    @Test
    void parseUserId_shouldThrowException_whenTokenMalformed() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String malformedToken = "header.payload"; // Missing signature
        
        assertThrows(Exception.class, () -> jwtHelper.parseUserId(malformedToken));
    }

    @Test
    void constructor_shouldAcceptExactly32ByteSecret() {
        String secret32 = "12345678901234567890123456789012"; // Exactly 32 bytes
        
        assertDoesNotThrow(() -> new JwtHelper(secret32));
    }

    @Test
    void constructor_shouldAcceptLongerThan32ByteSecret() {
        String longSecret = "this-is-a-very-very-very-long-secret-key-much-longer-than-32-bytes";
        
        assertDoesNotThrow(() -> new JwtHelper(longSecret));
    }

    @Test
    void createAndParse_shouldRoundTrip_forVariousUserIds() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        int[] testIds = {1, 10, 100, 1000, 10000, 99999};
        
        for (int userId : testIds) {
            String token = jwtHelper.createToken(userId);
            Integer parsedId = jwtHelper.parseUserId(token);
            assertEquals(userId, parsedId, "Round trip failed for userId: " + userId);
        }
    }

    @Test
    void parseUserId_shouldThrowException_whenTokenHasInvalidFormat() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        String[] invalidTokens = {
            "not-a-jwt",
            "single.part",
            "two.parts.only",
            "header.payload.signature.extra",
            "",
            "   ",
            "Bearer token"
        };
        
        for (String invalidToken : invalidTokens) {
            assertThrows(Exception.class, () -> jwtHelper.parseUserId(invalidToken), 
                "Should throw for invalid token: " + invalidToken);
        }
    }

    @Test
    void createToken_shouldNotIncludeExpiration() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String token = jwtHelper.createToken(5);
        
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        assertNull(claims.getExpiration(), "Token should not have expiration");
    }

    @Test
    void createToken_shouldUseHS256Algorithm() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String token = jwtHelper.createToken(1);
        
        // JWT header should indicate HS256
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
        
        // Decode header (first part) - Base64 URL without padding
        String header = parts[0];
        // Add padding if necessary
        while (header.length() % 4 != 0) {
            header += "=";
        }
        String headerJson = new String(java.util.Base64.getUrlDecoder().decode(header));
        assertTrue(headerJson.contains("HS256") || headerJson.contains("HmacSHA256"), "Token should use HS256 algorithm");
    }

    @Test
    void parseUserId_shouldHandleWhitespaceInToken() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String token = jwtHelper.createToken(5);
        String tokenWithWhitespace = "  " + token + "  ";
        
        // Should fail because whitespace is not trimmed in parseUserId
        assertThrows(Exception.class, () -> jwtHelper.parseUserId(tokenWithWhitespace));
    }

    @Test
    void createToken_shouldBeConsistent() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        
        // Create multiple tokens for the same user
        String token1 = jwtHelper.createToken(42);
        String token2 = jwtHelper.createToken(42);
        String token3 = jwtHelper.createToken(42);
        
        // All should be identical
        assertEquals(token1, token2);
        assertEquals(token2, token3);
    }

    @Test
    void twoInstances_withSameSecret_shouldCreateIdenticalTokens() {
        JwtHelper helper1 = new JwtHelper(VALID_SECRET);
        JwtHelper helper2 = new JwtHelper(VALID_SECRET);
        
        String token1 = helper1.createToken(10);
        String token2 = helper2.createToken(10);
        
        assertEquals(token1, token2);
    }

    @Test
    void twoInstances_withSameSecret_shouldParseEachOthersTokens() {
        JwtHelper helper1 = new JwtHelper(VALID_SECRET);
        JwtHelper helper2 = new JwtHelper(VALID_SECRET);
        
        String token = helper1.createToken(25);
        Integer userId = helper2.parseUserId(token);
        
        assertEquals(25, userId);
    }

    @Test
    void parseUserId_shouldThrowException_whenTokenTamperedWith() {
        JwtHelper jwtHelper = new JwtHelper(VALID_SECRET);
        String token = jwtHelper.createToken(5);
        
        // Tamper with the token by changing a character
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        
        assertThrows(Exception.class, () -> jwtHelper.parseUserId(tamperedToken));
    }
}
