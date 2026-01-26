package com.example.auth.audit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_logs_actor_user_id", columnList = "actor_user_id"),
        @Index(name = "idx_logs_entity", columnList = "entity, entity_id"),
        @Index(name = "idx_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_logs_action", columnList = "action")
    }
)
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // QUI (copie pratique, sans FK)
    @Column(name = "actor_user_id")
    private Integer actorUserId;

    @Column(name = "actor_name", length = 255)
    private String actorName;

    @Column(name = "actor_mail", length = 255)
    private String actorMail;

    // QUOI
    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Column(name = "entity", nullable = false, length = 60)
    private String entity;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "message", length = 500)
    private String message;

    // CONTEXTE
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // QUAND
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Integer actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorMail() {
        return actorMail;
    }

    public void setActorMail(String actorMail) {
        this.actorMail = actorMail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
