package com.example.auth.inscription.adapters.out;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UsersJpaEntity 
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "text")
    private String name;

    @Column(nullable = false)
    private String mail;

    @Column(name = "psw_hash", nullable = false, columnDefinition = "text")
    private String pswHash;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "public_key", columnDefinition = "text")
    private String publicKey;

    @Column(name = "vault_key", columnDefinition = "text")
    private String vaultKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getPswHash() { return pswHash; }
    public void setPswHash(String pswHash) { this.pswHash = pswHash; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getVaultKey() { return vaultKey; }
    public void setVaultKey(String vaultKey) { this.vaultKey = vaultKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
