package com.example.auth.login.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "signature_transactions",
    indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_sig_video_hash", columnList = "video_hash")
    }
)
public class SignatureTransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "transaction_send_to", nullable = false, columnDefinition = "text")
    private String transactionSendTo;

    @Column(name = "video_name", nullable = false, length = 255)
    private String videoName;

    @Column(name = "montant_transaction", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTransaction;

    @Column(name = "video_hash", nullable = false, columnDefinition = "text")
    private String videoHash;

    @Column(name = "path_video", nullable = false, columnDefinition = "text")
    private String pathVideo;

    @Column(name = "expired_video", nullable = false)
    private LocalDateTime expiredVideo;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "public_key", nullable = false, columnDefinition = "text")
    private String publicKey;

    @Column(name = "signature", nullable = false, columnDefinition = "text")
    private String signature;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /* ==========================
       Lifecycle hooks
       ========================== */

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (signedAt == null) signedAt = LocalDateTime.now();
    }

    /* ==========================
       Getters & Setters
       ========================== */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTransactionSendTo() {
        return transactionSendTo;
    }

    public void setTransactionSendTo(String transactionSendTo) {
        this.transactionSendTo = transactionSendTo;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public BigDecimal getMontantTransaction() {
        return montantTransaction;
    }

    public void setMontantTransaction(BigDecimal montantTransaction) {
        this.montantTransaction = montantTransaction;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public void setVideoHash(String videoHash) {
        this.videoHash = videoHash;
    }

    public String getPathVideo() {
        return pathVideo;
    }

    public void setPathVideo(String pathVideo) {
        this.pathVideo = pathVideo;
    }

    public LocalDateTime getExpiredVideo() {
        return expiredVideo;
    }

    public void setExpiredVideo(LocalDateTime expiredVideo) {
        this.expiredVideo = expiredVideo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
