package com.example.auth.mfa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité JPA pour stocker les codes MFA temporaires.
 * Les codes sont générés lors de l'authentification OAuth2 et ont une durée de vie limitée.
 */
@Entity
@Table(name = "mfa_codes", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class MfaCodeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Constructeurs
    public MfaCodeJpaEntity() {}

    public MfaCodeJpaEntity(Integer userId, String code, LocalDateTime expiresAt, String sessionToken) {
        this.userId = userId;
        this.code = code;
        this.expiresAt = expiresAt;
        this.sessionToken = sessionToken;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Vérifie si le code MFA est encore valide
     * @return true si le code n'est pas utilisé et n'a pas expiré
     */
    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiresAt);
    }
}
