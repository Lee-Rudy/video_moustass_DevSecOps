package com.example.auth.userTest;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.user.UsersController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UsersController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UsersControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SpringDataUsersRepository userRepo;

    @Test
    void listNonAdmin_shouldReturnListOfNonAdminUsers() throws Exception {
        UsersJpaEntity user1 = createUser(1, "Alice", "alice@test.com", false);
        UsersJpaEntity user2 = createUser(2, "Bob", "bob@test.com", false);
        UsersJpaEntity user3 = createUser(3, "Charlie", "charlie@test.com", false);

        List<UsersJpaEntity> nonAdminUsers = Arrays.asList(user1, user2, user3);

        when(userRepo.findByIsAdminFalse()).thenReturn(nonAdminUsers);

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bob"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("Charlie"));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldReturnEmptyList_whenNoNonAdminUsers() throws Exception {
        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldNotIncludeAdminUsers() throws Exception {
        UsersJpaEntity user1 = createUser(1, "Alice", "alice@test.com", false);
        UsersJpaEntity user2 = createUser(2, "Bob", "bob@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldReturnOnlyIdAndName() throws Exception {
        UsersJpaEntity user = createUser(1, "Alice", "alice@test.com", false);
        user.setVaultKey("vault-key-alice");
        user.setPublicKey("public-key-alice");
        user.setPswHash("hashed-password");

        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].mail").doesNotExist())
                .andExpect(jsonPath("$[0].vaultKey").doesNotExist())
                .andExpect(jsonPath("$[0].publicKey").doesNotExist())
                .andExpect(jsonPath("$[0].pswHash").doesNotExist());

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldHandleUserWithNullName() throws Exception {
        UsersJpaEntity user1 = createUser(1, null, "user1@test.com", false);
        UsersJpaEntity user2 = createUser(2, "Bob", "bob@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(""))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldCallRepositoryOnce() throws Exception {
        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk());

        verify(userRepo, times(1)).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldWorkForDifferentRequestingUsers() throws Exception {
        UsersJpaEntity user = createUser(1, "Alice", "alice@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.singletonList(user));

        // Request 1: userId = 5
        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));

        // Request 2: userId = 10
        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));

        verify(userRepo, times(2)).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldReturnUsersInCorrectOrder() throws Exception {
        UsersJpaEntity user1 = createUser(10, "Alice", "alice@test.com", false);
        UsersJpaEntity user2 = createUser(20, "Bob", "bob@test.com", false);
        UsersJpaEntity user3 = createUser(30, "Charlie", "charlie@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Arrays.asList(user1, user2, user3));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(20))
                .andExpect(jsonPath("$[2].id").value(30));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldHandleMultipleUsersWithNullNames() throws Exception {
        UsersJpaEntity user1 = createUser(1, null, "user1@test.com", false);
        UsersJpaEntity user2 = createUser(2, null, "user2@test.com", false);
        UsersJpaEntity user3 = createUser(3, "Charlie", "charlie@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Arrays.asList(user1, user2, user3));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value(""))
                .andExpect(jsonPath("$[1].name").value(""))
                .andExpect(jsonPath("$[2].name").value("Charlie"));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldHandleUserWithEmptyName() throws Exception {
        UsersJpaEntity user = createUser(1, "", "user@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(""));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldHandleUserWithWhitespaceName() throws Exception {
        UsersJpaEntity user = createUser(1, "   ", "user@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("   "));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldReturnCorrectDtoStructure() throws Exception {
        UsersJpaEntity user = createUser(42, "TestUser", "test@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").isString())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].name").value("TestUser"));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldHandleLargeNumberOfUsers() throws Exception {
        List<UsersJpaEntity> users = Arrays.asList(
            createUser(1, "User1", "user1@test.com", false),
            createUser(2, "User2", "user2@test.com", false),
            createUser(3, "User3", "user3@test.com", false),
            createUser(4, "User4", "user4@test.com", false),
            createUser(5, "User5", "user5@test.com", false),
            createUser(6, "User6", "user6@test.com", false),
            createUser(7, "User7", "user7@test.com", false),
            createUser(8, "User8", "user8@test.com", false),
            createUser(9, "User9", "user9@test.com", false),
            createUser(10, "User10", "user10@test.com", false)
        );

        when(userRepo.findByIsAdminFalse()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[9].name").value("User10"));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldHandleUsersWithSpecialCharactersInName() throws Exception {
        UsersJpaEntity user1 = createUser(1, "François", "francois@test.com", false);
        UsersJpaEntity user2 = createUser(2, "José María", "jose@test.com", false);
        UsersJpaEntity user3 = createUser(3, "李明", "li@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Arrays.asList(user1, user2, user3));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("François"))
                .andExpect(jsonPath("$[1].name").value("José María"))
                .andExpect(jsonPath("$[2].name").value("李明"));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldHandleUsersWithLongNames() throws Exception {
        String longName = "ThisIsAVeryLongNameThatExceedsNormalLengthAndTestsTheBoundaries";
        UsersJpaEntity user = createUser(1, longName, "user@test.com", false);

        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(longName));

        verify(userRepo).findByIsAdminFalse();
    }

    @Test
    void listNonAdmin_shouldReturnContentTypeJson() throws Exception {
        when(userRepo.findByIsAdminFalse()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users")
                        .requestAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(userRepo).findByIsAdminFalse();
    }

    // Helper method
    private UsersJpaEntity createUser(int id, String name, String mail, boolean isAdmin) {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(id);
        user.setName(name);
        user.setMail(mail);
        user.setAdmin(isAdmin);
        return user;
    }
}
