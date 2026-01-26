package com.example.auth.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Génère des JWT avec l'id utilisateur (sans expiration).
 * L'userId en claim permet de l'utiliser plus tard pour signature_transactions.
 */
@Component
public class JwtHelper {

    private final SecretKey key;

    public JwtHelper(@Value("${auth.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("auth.jwt.secret doit faire au moins 32 octets (HS256)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Integer userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .signWith(key)
                .compact();
    }

    /** Extrait le userId du JWT (Bearer). Lance si token invalide. */
    public Integer parseUserId(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Integer.class);
    }
}
