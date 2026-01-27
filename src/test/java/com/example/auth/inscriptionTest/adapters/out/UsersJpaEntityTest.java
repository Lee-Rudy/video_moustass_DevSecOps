package com.example.auth.inscriptionTest.adapters.out;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsersJpaEntityTest {

    @Test
    void shouldCreateEmptyEntity() {
        UsersJpaEntity entity = new UsersJpaEntity();
        assertNotNull(entity);
    }

    @Test
    void shouldSetAndGetId() {
        UsersJpaEntity entity = new UsersJpaEntity();
        entity.setId(123);
        assertEquals(123, entity.getId());
    }

    @Test
    void shouldSetAndGetName() {
        UsersJpaEntity entity = new UsersJpaEntity();
        entity.setName("Alice");
        assertEquals("Alice", entity.getName());
    }

    @Test
    void shouldSetAndGetMail() {
        UsersJpaEntity entity = new UsersJpaEntity();
        entity.setMail("alice@test.com");
        assertEquals("alice@test.com", entity.getMail());
    }

    @Test
    void shouldSetAndGetPswHash() {
        UsersJpaEntity entity = new UsersJpaEntity();
        String hash = "$2a$10$HASH123";
        entity.setPswHash(hash);
        assertEquals(hash, entity.getPswHash());
    }

    @Test
    void shouldSetAndGetAdmin() {
        UsersJpaEntity entity = new UsersJpaEntity();
        
        entity.setAdmin(false);
        assertFalse(entity.isAdmin());
        
        entity.setAdmin(true);
        assertTrue(entity.isAdmin());
    }

    @Test
    void shouldSetAndGetPublicKey() {
        UsersJpaEntity entity = new UsersJpaEntity();
        String publicKey = "PUBLIC_KEY_XYZ";
        entity.setPublicKey(publicKey);
        assertEquals(publicKey, entity.getPublicKey());
    }

    @Test
    void shouldSetAndGetVaultKey() {
        UsersJpaEntity entity = new UsersJpaEntity();
        String vaultKey = "vault-key-abc";
        entity.setVaultKey(vaultKey);
        assertEquals(vaultKey, entity.getVaultKey());
    }

    @Test
    void shouldSetAllFieldsCorrectly() {
        UsersJpaEntity entity = new UsersJpaEntity();
        
        entity.setId(1);
        entity.setName("Bob");
        entity.setMail("bob@example.com");
        entity.setPswHash("$2a$10$BOBHASH");
        entity.setAdmin(true);
        entity.setPublicKey("BOB_PUBLIC_KEY");
        entity.setVaultKey("bob-vault-key");

        assertEquals(1, entity.getId());
        assertEquals("Bob", entity.getName());
        assertEquals("bob@example.com", entity.getMail());
        assertEquals("$2a$10$BOBHASH", entity.getPswHash());
        assertTrue(entity.isAdmin());
        assertEquals("BOB_PUBLIC_KEY", entity.getPublicKey());
        assertEquals("bob-vault-key", entity.getVaultKey());
    }

    @Test
    void shouldHandleNullValues() {
        UsersJpaEntity entity = new UsersJpaEntity();
        
        entity.setName(null);
        entity.setMail(null);
        entity.setPswHash(null);
        entity.setPublicKey(null);
        entity.setVaultKey(null);

        assertNull(entity.getName());
        assertNull(entity.getMail());
        assertNull(entity.getPswHash());
        assertNull(entity.getPublicKey());
        assertNull(entity.getVaultKey());
    }

    @Test
    void shouldDefaultAdminToFalse() {
        UsersJpaEntity entity = new UsersJpaEntity();
        // Par défaut, isAdmin devrait être false (valeur par défaut du boolean)
        assertFalse(entity.isAdmin());
    }

    @Test
    void shouldCreateRegularUser() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(10);
        user.setName("Regular User");
        user.setMail("user@test.com");
        user.setPswHash("$2a$10$USERHASH");
        user.setAdmin(false);
        user.setPublicKey("USER_PUB");
        user.setVaultKey("user-vault");

        assertEquals(10, user.getId());
        assertEquals("Regular User", user.getName());
        assertFalse(user.isAdmin());
        assertEquals("USER_PUB", user.getPublicKey());
    }

    @Test
    void shouldCreateAdminUser() {
        UsersJpaEntity admin = new UsersJpaEntity();
        admin.setId(99);
        admin.setName("Admin User");
        admin.setMail("admin@test.com");
        admin.setPswHash("$2a$10$ADMINHASH");
        admin.setAdmin(true);
        admin.setPublicKey("ADMIN_PUB");
        admin.setVaultKey("admin-vault");

        assertEquals(99, admin.getId());
        assertEquals("Admin User", admin.getName());
        assertTrue(admin.isAdmin());
        assertEquals("ADMIN_PUB", admin.getPublicKey());
    }

    @Test
    void shouldUpdateExistingEntity() {
        UsersJpaEntity entity = new UsersJpaEntity();
        entity.setId(1);
        entity.setName("Original Name");
        entity.setMail("original@test.com");

        // Update
        entity.setName("Updated Name");
        entity.setMail("updated@test.com");
        entity.setAdmin(true);

        assertEquals("Updated Name", entity.getName());
        assertEquals("updated@test.com", entity.getMail());
        assertTrue(entity.isAdmin());
        assertEquals(1, entity.getId()); // ID should not change
    }
}
