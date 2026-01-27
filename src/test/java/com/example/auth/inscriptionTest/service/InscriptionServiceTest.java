package com.example.auth.inscriptionTest.service;

import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.out.InscriptionRepository;
import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import com.example.auth.inscription.service.InscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InscriptionServiceTest {

    private InscriptionRepository repo;
    private UserKeyVaultPort vault;
    private BCryptPasswordEncoder encoder;
    private InscriptionService service;

    private String password = "PasswordA1";

    @BeforeEach
    void setUp() {
        repo = mock(InscriptionRepository.class);
        vault = mock(UserKeyVaultPort.class);
        encoder = new BCryptPasswordEncoder();
        service = new InscriptionService(repo, vault, encoder);
    }

    @Test
    void saveUser_shouldHashPassword_createVaultKey_exportPublicKey_andSave() {
        // given
        Users input = new Users(0, "Alice", "alice@gmail.com", "PasswordA1", false, null, null);

        when(vault.exportPublicKey(anyString())).thenReturn("PUBLIC_KEY_VALUE");

        // Repository renvoie l'objet sauvegardé (ici on renvoie l’argument tel quel)
        when(repo.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Users saved = service.saveUser(input);

        // then: vault_key doit être défini
        assertNotNull(saved.getVaultKey());
        assertTrue(saved.getVaultKey().startsWith("user-signing-"));

        // then: public_key doit être défini
        assertEquals("PUBLIC_KEY_VALUE", saved.getPublicKey());

        // then: password doit être hashé (différent du clair)

        assertNotEquals(password, saved.getPsw());
        assertTrue(encoder.matches(password, saved.getPsw()));

        // then: Vault appelé correctement
        verify(vault, times(1)).createSigningKey(saved.getVaultKey());
        verify(vault, times(1)).exportPublicKey(saved.getVaultKey());

        // then: repo.save appelé avec un Users contenant vault_key/public_key
        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(repo, times(1)).save(captor.capture());
        Users toSave = captor.getValue();

        assertEquals(saved.getVaultKey(), toSave.getVaultKey());
        assertEquals("PUBLIC_KEY_VALUE", toSave.getPublicKey());
        assertTrue(encoder.matches(password, toSave.getPsw()));
    }

    @Test
    void saveUser_shouldFail_whenVaultPublicKeyExportFails() {
        Users input = new Users(0, "Alice", "alice@gmail.com", "PasswordA1", false, null, null);

        when(vault.exportPublicKey(anyString())).thenThrow(new IllegalStateException("export failed"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.saveUser(input));
        assertTrue(ex.getMessage().contains("export"));

        // assure qu'on a quand même essayé de créer une clé avant l’export
        verify(vault, times(1)).createSigningKey(anyString());

        // assure qu'on ne sauvegarde pas en DB si Vault échoue (bonne pratique)
        verify(repo, never()).save(any());
    }

    @Test
    void getAllUsers_shouldReturnAllUsersFromRepository() {
        Users user1 = new Users(1, "Alice", "alice@test.com", "HASH1", false, "PUB1", "vault1");
        Users user2 = new Users(2, "Bob", "bob@test.com", "HASH2", false, "PUB2", "vault2");
        Users admin = new Users(3, "Admin", "admin@test.com", "HASH3", true, "PUB3", "vault3");

        when(repo.findAll()).thenReturn(java.util.Arrays.asList(user1, user2, admin));

        java.util.List<Users> result = service.getAllUsers();

        assertEquals(3, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());
        assertEquals("Admin", result.get(2).getName());
        assertTrue(result.get(2).getIsAdmin());
        verify(repo, times(1)).findAll();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(repo.findAll()).thenReturn(java.util.Collections.emptyList());

        java.util.List<Users> result = service.getAllUsers();

        assertTrue(result.isEmpty());
        verify(repo, times(1)).findAll();
    }

    @Test
    void deleteUser_shouldCallRepositoryDeleteById() {
        Integer userId = 5;

        doNothing().when(repo).deleteById(userId);

        service.deleteUser(userId);

        verify(repo, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_shouldCallRepositoryWithCorrectId() {
        Integer userId = 123;

        doNothing().when(repo).deleteById(anyInt());

        service.deleteUser(userId);

        verify(repo).deleteById(eq(123));
    }

    @Test
    void saveUser_shouldHandleAdminUser() {
        Users adminInput = new Users(0, "SuperAdmin", "admin@test.com", "AdminPass123", true, null, null);

        when(vault.exportPublicKey(anyString())).thenReturn("ADMIN_PUBLIC_KEY");
        when(repo.save(any(Users.class))).thenAnswer(invocation -> {
            Users u = invocation.getArgument(0);
            u.setIdUsers(99);
            return u;
        });

        Users saved = service.saveUser(adminInput);

        assertEquals("SuperAdmin", saved.getName());
        assertTrue(saved.getIsAdmin());
        assertEquals("ADMIN_PUBLIC_KEY", saved.getPublicKey());
        assertNotNull(saved.getVaultKey());
        assertTrue(encoder.matches("AdminPass123", saved.getPsw()));

        verify(vault).createSigningKey(anyString());
        verify(vault).exportPublicKey(anyString());
        verify(repo).save(any(Users.class));
    }
}
