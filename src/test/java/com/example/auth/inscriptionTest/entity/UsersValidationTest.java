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
}
