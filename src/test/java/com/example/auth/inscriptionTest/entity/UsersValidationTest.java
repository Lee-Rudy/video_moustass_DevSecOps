package com.example.auth.inscriptionTest.entity;

import org.junit.jupiter.api.Test;
import com.example.auth.inscription.entity.Users;

import static org.junit.jupiter.api.Assertions.*;

class UsersValidationTest 
{

    @Test
    void shouldRejectEmptyName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "   ", "a@b.com", "PasswordA1", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("nom"));
    }

    @Test
    void shouldRejectInvalidEmail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "not-an-email", "PasswordA1", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("mail"));
    }

    @Test
    void shouldRejectShortPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "alice@gmail.com", "Abc123", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("8"));
    }

    @Test
    void shouldRejectPasswordWithoutUppercase() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "alice@gmail.com", "password12345", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("majuscule"));
    }

    @Test
    void shouldAcceptValidUser() {
        Users u = new Users(0, "Alice", "alice@gmail.com", "PasswordA1", false, null, null);
        assertEquals("Alice", u.getName());
        assertEquals("alice@gmail.com", u.getMail());
        assertEquals("PasswordA1", u.getPsw());
        assertFalse(u.getIsAdmin());
    }

    @Test
    void shouldRejectNullName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, null, "a@b.com", "PasswordA1", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("nom"));
    }

    @Test
    void shouldRejectNullEmail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", null, "PasswordA1", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("mail"));
    }

    @Test
    void shouldRejectEmptyEmail() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "   ", "PasswordA1", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("mail"));
    }

    @Test
    void shouldRejectNullPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "alice@test.com", null, false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("mot de passe"));
    }

    @Test
    void shouldRejectEmptyPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "alice@test.com", "   ", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("mot de passe"));
    }

    @Test
    void shouldTrimAndLowercaseEmail() {
        Users u = new Users(0, "Alice", "  ALICE@GMAIL.COM  ", "PasswordA1", false, null, null);
        assertEquals("alice@gmail.com", u.getMail());
    }

    @Test
    void shouldTrimName() {
        Users u = new Users(0, "  Alice  ", "alice@test.com", "PasswordA1", false, null, null);
        assertEquals("Alice", u.getName());
    }

    @Test
    void shouldAcceptAdminUser() {
        Users admin = new Users(1, "Admin", "admin@test.com", "AdminPassword123", true, "pubkey", "vaultkey");
        assertTrue(admin.getIsAdmin());
        assertEquals("Admin", admin.getName());
        assertEquals("admin@test.com", admin.getMail());
        assertEquals("pubkey", admin.getPublicKey());
        assertEquals("vaultkey", admin.getVaultKey());
    }

    @Test
    void shouldAcceptMinimumValidPassword() {
        // Exactly 8 characters with uppercase
        Users u = new Users(0, "User", "user@test.com", "Password", false, null, null);
        assertEquals("Password", u.getPsw());
    }

    @Test
    void shouldAcceptLongPassword() {
        String longPassword = "PasswordWithManyCharacters123456789ABCDEFGHIJK";
        Users u = new Users(0, "User", "user@test.com", longPassword, false, null, null);
        assertEquals(longPassword, u.getPsw());
    }

    @Test
    void shouldRejectEmailWithoutAtSign() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "alicegmail.com", "PasswordA1", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("mail"));
    }

    @Test
    void shouldRejectEmailWithoutDomain() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Users(0, "Alice", "alice@", "PasswordA1", false, null, null)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("mail"));
    }

    @Test
    void shouldSettersWork() {
        Users u = new Users();
        u.setIdUsers(10);
        u.setName("Bob");
        u.setMail("bob@test.com");
        u.setPsw("NewPassword123");
        u.setIsAdmin(true);
        u.setPublicKey("newpub");
        u.setVaultKey("newvault");

        assertEquals(10, u.getIdUsers());
        assertEquals("Bob", u.getName());
        assertEquals("bob@test.com", u.getMail());
        assertEquals("NewPassword123", u.getPsw());
        assertTrue(u.getIsAdmin());
        assertEquals("newpub", u.getPublicKey());
        assertEquals("newvault", u.getVaultKey());
    }

    @Test
    void shouldAcceptValidEmailVariants() {
        // Test différents formats d'email valides
        assertDoesNotThrow(() -> new Users(0, "U1", "user@domain.com", "Password1", false, null, null));
        assertDoesNotThrow(() -> new Users(0, "U2", "user.name@domain.com", "Password1", false, null, null));
        assertDoesNotThrow(() -> new Users(0, "U3", "user+tag@domain.co.uk", "Password1", false, null, null));
        assertDoesNotThrow(() -> new Users(0, "U4", "user_123@test-domain.org", "Password1", false, null, null));
    }
}
