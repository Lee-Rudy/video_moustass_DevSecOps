package com.example.auth.inscriptionTest.adapters.out;

import com.example.auth.inscription.adapters.out.JpaInscriptionRepositoryAdapter;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JpaInscriptionRepositoryAdapterTest {

    private SpringDataUsersRepository springRepo;
    private JpaInscriptionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springRepo = mock(SpringDataUsersRepository.class);
        adapter = new JpaInscriptionRepositoryAdapter(springRepo);
    }

    @Test
    void save_shouldConvertDomainToJpaAndSave() {
        Users domainUser = new Users(0, "Alice", "alice@test.com", "HASH123", false, "PUBKEY", "vault-key");

        UsersJpaEntity jpaEntity = new UsersJpaEntity();
        jpaEntity.setId(10);
        jpaEntity.setName("Alice");
        jpaEntity.setMail("alice@test.com");
        jpaEntity.setPswHash("HASH123");
        jpaEntity.setAdmin(false);
        jpaEntity.setPublicKey("PUBKEY");
        jpaEntity.setVaultKey("vault-key");

        when(springRepo.save(any(UsersJpaEntity.class))).thenReturn(jpaEntity);

        Users result = adapter.save(domainUser);

        assertNotNull(result);
        assertEquals(10, result.getIdUsers());
        assertEquals("Alice", result.getName());
        assertEquals("alice@test.com", result.getMail());
        assertEquals("HASH123", result.getPsw());
        assertFalse(result.getIsAdmin());
        assertEquals("PUBKEY", result.getPublicKey());
        assertEquals("vault-key", result.getVaultKey());

        verify(springRepo, times(1)).save(any(UsersJpaEntity.class));
    }

    @Test
    void save_shouldHandleAdminUser() {
        Users adminUser = new Users(0, "Admin", "admin@test.com", "ADMINHASH", true, "ADMINPUB", "admin-vault");

        UsersJpaEntity savedEntity = new UsersJpaEntity();
        savedEntity.setId(99);
        savedEntity.setName("Admin");
        savedEntity.setMail("admin@test.com");
        savedEntity.setPswHash("ADMINHASH");
        savedEntity.setAdmin(true);
        savedEntity.setPublicKey("ADMINPUB");
        savedEntity.setVaultKey("admin-vault");

        when(springRepo.save(any(UsersJpaEntity.class))).thenReturn(savedEntity);

        Users result = adapter.save(adminUser);

        assertTrue(result.getIsAdmin());
        assertEquals("Admin", result.getName());
        verify(springRepo).save(any(UsersJpaEntity.class));
    }

    @Test
    void findAll_shouldReturnAllUsersConvertedToDomain() {
        UsersJpaEntity entity1 = new UsersJpaEntity();
        entity1.setId(1);
        entity1.setName("Alice");
        entity1.setMail("alice@test.com");
        entity1.setPswHash("HASH1");
        entity1.setAdmin(false);
        entity1.setPublicKey("PUB1");
        entity1.setVaultKey("vault1");

        UsersJpaEntity entity2 = new UsersJpaEntity();
        entity2.setId(2);
        entity2.setName("Bob");
        entity2.setMail("bob@test.com");
        entity2.setPswHash("HASH2");
        entity2.setAdmin(false);
        entity2.setPublicKey("PUB2");
        entity2.setVaultKey("vault2");

        UsersJpaEntity entity3 = new UsersJpaEntity();
        entity3.setId(3);
        entity3.setName("Admin");
        entity3.setMail("admin@test.com");
        entity3.setPswHash("HASH3");
        entity3.setAdmin(true);
        entity3.setPublicKey("PUB3");
        entity3.setVaultKey("vault3");

        when(springRepo.findAll()).thenReturn(Arrays.asList(entity1, entity2, entity3));

        List<Users> result = adapter.findAll();

        assertNotNull(result);
        assertEquals(3, result.size());
        
        assertEquals("Alice", result.get(0).getName());
        assertEquals("alice@test.com", result.get(0).getMail());
        assertFalse(result.get(0).getIsAdmin());
        
        assertEquals("Bob", result.get(1).getName());
        assertFalse(result.get(1).getIsAdmin());
        
        assertEquals("Admin", result.get(2).getName());
        assertTrue(result.get(2).getIsAdmin());

        verify(springRepo, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoUsers() {
        when(springRepo.findAll()).thenReturn(Collections.emptyList());

        List<Users> result = adapter.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(springRepo, times(1)).findAll();
    }

    @Test
    void deleteById_shouldCallSpringRepoDeleteById() {
        Integer userId = 5;

        doNothing().when(springRepo).deleteById(userId);

        adapter.deleteById(userId);

        verify(springRepo, times(1)).deleteById(userId);
    }

    @Test
    void deleteById_shouldCallWithCorrectId() {
        doNothing().when(springRepo).deleteById(anyInt());

        adapter.deleteById(123);

        verify(springRepo).deleteById(eq(123));
        verify(springRepo, never()).deleteById(eq(456));
    }

    @Test
    void save_shouldPreserveAllFields() {
        Users user = new Users(0, "Test User", "test@example.com", "password123", true, "public-key-xyz", "vault-abc");

        UsersJpaEntity savedEntity = new UsersJpaEntity();
        savedEntity.setId(50);
        savedEntity.setName("Test User");
        savedEntity.setMail("test@example.com");
        savedEntity.setPswHash("password123");
        savedEntity.setAdmin(true);
        savedEntity.setPublicKey("public-key-xyz");
        savedEntity.setVaultKey("vault-abc");

        when(springRepo.save(any(UsersJpaEntity.class))).thenReturn(savedEntity);

        Users result = adapter.save(user);

        assertEquals(50, result.getIdUsers());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getMail());
        assertEquals("password123", result.getPsw());
        assertTrue(result.getIsAdmin());
        assertEquals("public-key-xyz", result.getPublicKey());
        assertEquals("vault-abc", result.getVaultKey());
    }
}
