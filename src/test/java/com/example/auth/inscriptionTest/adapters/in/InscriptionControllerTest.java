package com.example.auth.inscriptionTest.adapters.in;

import com.example.auth.inscription.adapters.in.InscriptionController;
import com.example.auth.inscription.entity.Users;
import com.example.auth.inscription.ports.in.InscriptionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = InscriptionController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class InscriptionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    InscriptionUseCase useCase;

    @Test
    void createUser_shouldReturnSavedUser() throws Exception {
        Users returned = new Users(10, "Alice", "alice@gmail.com", "HASHEDPASSWORD123", false, "PUB", "vault-key-1");

        when(useCase.saveUser(any(Users.class))).thenReturn(returned);

        String json = """
                {
                  "name": "Alice",
                  "mail": "alice@gmail.com",
                  "psw": "PasswordA1",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsers").value(10))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.mail").value("alice@gmail.com"))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.publicKey").value("PUB"))
                .andExpect(jsonPath("$.vaultKey").value("vault-key-1"));

        verify(useCase).saveUser(any(Users.class));
    }

    @Test
    void createUser_shouldCreateAdmin_whenIsAdminTrue() throws Exception {
        Users adminReturned = new Users(20, "Admin", "admin@test.com", "HASH", true, "PUBKEY", "vault-admin");

        when(useCase.saveUser(any(Users.class))).thenReturn(adminReturned);

        String json = """
                {
                  "name": "Admin",
                  "mail": "admin@test.com",
                  "psw": "AdminPass123",
                  "isAdmin": true
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.name").value("Admin"));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        Users user1 = new Users(1, "Alice", "alice@test.com", "HASH1", false, "PUB1", "vault1");
        Users user2 = new Users(2, "Bob", "bob@test.com", "HASH2", false, "PUB2", "vault2");
        Users admin = new Users(3, "Admin", "admin@test.com", "HASH3", true, "PUB3", "vault3");

        List<Users> usersList = Arrays.asList(user1, user2, admin);

        when(useCase.getAllUsers()).thenReturn(usersList);

        mockMvc.perform(get("/api/inscription/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].mail").value("alice@test.com"))
                .andExpect(jsonPath("$[0].isAdmin").value(false))
                .andExpect(jsonPath("$[1].name").value("Bob"))
                .andExpect(jsonPath("$[2].name").value("Admin"))
                .andExpect(jsonPath("$[2].isAdmin").value(true));

        verify(useCase).getAllUsers();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() throws Exception {
        when(useCase.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/inscription/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(useCase).getAllUsers();
    }

    @Test
    void deleteUser_shouldReturnNoContent_whenUserDeleted() throws Exception {
        doNothing().when(useCase).deleteUser(5);

        mockMvc.perform(delete("/api/inscription/users/5"))
                .andExpect(status().isNoContent());

        verify(useCase).deleteUser(5);
    }

    @Test
    void deleteUser_shouldDeleteCorrectUser() throws Exception {
        doNothing().when(useCase).deleteUser(123);

        mockMvc.perform(delete("/api/inscription/users/123"))
                .andExpect(status().isNoContent());

        verify(useCase).deleteUser(eq(123));
        verify(useCase, never()).deleteUser(eq(456));
    }

    @Test
    void createUser_shouldHandleNullPublicKey() throws Exception {
        Users returned = new Users(10, "Alice", "alice@gmail.com", "HASH", false, null, "vault-key");

        when(useCase.saveUser(any(Users.class))).thenReturn(returned);

        String json = """
                {
                  "name": "Alice",
                  "mail": "alice@gmail.com",
                  "psw": "Password123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKey").isEmpty());

        verify(useCase).saveUser(any(Users.class));
    }

    @Test
    void createUser_shouldHandleNullVaultKey() throws Exception {
        Users returned = new Users(10, "Alice", "alice@gmail.com", "HASH", false, "PUB", null);

        when(useCase.saveUser(any(Users.class))).thenReturn(returned);

        String json = """
                {
                  "name": "Alice",
                  "mail": "alice@gmail.com",
                  "psw": "Password123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vaultKey").isEmpty());

        verify(useCase).saveUser(any(Users.class));
    }

    @Test
    void createUser_shouldPassCorrectParametersToUseCase() throws Exception {
        when(useCase.saveUser(any(Users.class))).thenAnswer(inv -> {
            Users input = inv.getArgument(0);
            return new Users(50, input.getName(), input.getMail(), "HASHED", input.getIsAdmin(), "PUB", "vault");
        });

        String json = """
                {
                  "name": "TestUser",
                  "mail": "test@example.com",
                  "psw": "TestPass123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestUser"))
                .andExpect(jsonPath("$.mail").value("test@example.com"));

        verify(useCase).saveUser(argThat(u -> 
            u.getName().equals("TestUser") && 
            u.getMail().equals("test@example.com") &&
            u.getPsw().equals("TestPass123") &&
            !u.getIsAdmin()
        ));
    }

    @Test
    void createUser_shouldSetIdToZero_beforeSaving() throws Exception {
        when(useCase.saveUser(any(Users.class))).thenAnswer(inv -> {
            Users input = inv.getArgument(0);
            return new Users(100, input.getName(), input.getMail(), "HASH", input.getIsAdmin(), "PUB", "vault");
        });

        String json = """
                {
                  "name": "NewUser",
                  "mail": "new@example.com",
                  "psw": "NewPass123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(useCase).saveUser(argThat(u -> u.getIdUsers() == 0));
    }

    @Test
    void createUser_shouldSetPublicKeyAndVaultKeyToNull() throws Exception {
        when(useCase.saveUser(any(Users.class))).thenAnswer(inv -> inv.getArgument(0));

        String json = """
                {
                  "name": "User",
                  "mail": "user@example.com",
                  "psw": "UserPass123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(useCase).saveUser(argThat(u -> 
            u.getPublicKey() == null && u.getVaultKey() == null
        ));
    }

    @Test
    void getAllUsers_shouldMapAllFieldsCorrectly() throws Exception {
        Users user = new Users(99, "CompleteUser", "complete@test.com", "HASH", false, "PUBLIC_KEY_DATA", "vault-key-data");

        when(useCase.getAllUsers()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/inscription/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsers").value(99))
                .andExpect(jsonPath("$[0].name").value("CompleteUser"))
                .andExpect(jsonPath("$[0].mail").value("complete@test.com"))
                .andExpect(jsonPath("$[0].isAdmin").value(false))
                .andExpect(jsonPath("$[0].publicKey").value("PUBLIC_KEY_DATA"))
                .andExpect(jsonPath("$[0].vaultKey").value("vault-key-data"));

        verify(useCase).getAllUsers();
    }

    @Test
    void deleteUser_shouldAcceptDifferentIdFormats() throws Exception {
        doNothing().when(useCase).deleteUser(anyInt());

        // Test avec ID simple
        mockMvc.perform(delete("/api/inscription/users/1"))
                .andExpect(status().isNoContent());

        // Test avec ID grand
        mockMvc.perform(delete("/api/inscription/users/999999"))
                .andExpect(status().isNoContent());

        verify(useCase).deleteUser(1);
        verify(useCase).deleteUser(999999);
    }

    @Test
    void createUser_shouldReturnCorrectResponseStructure() throws Exception {
        Users user = new Users(42, "StructTest", "struct@test.com", "HASH", false, "PUB", "vault");

        when(useCase.saveUser(any())).thenReturn(user);

        String json = """
                {
                  "name": "StructTest",
                  "mail": "struct@test.com",
                  "psw": "StructPass123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.idUsers").isNumber())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.mail").isString())
                .andExpect(jsonPath("$.isAdmin").isBoolean());
    }

    @Test
    void getAllUsers_shouldCallUseCaseExactlyOnce() throws Exception {
        when(useCase.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/inscription/users"))
                .andExpect(status().isOk());

        verify(useCase, times(1)).getAllUsers();
        verify(useCase, only()).getAllUsers();
    }

    @Test
    void createUser_shouldHandleSpecialCharactersInName() throws Exception {
        Users user = new Users(1, "François José", "francois@test.com", "HASH", false, "PUB", "vault");

        when(useCase.saveUser(any())).thenReturn(user);

        String json = """
                {
                  "name": "François José",
                  "mail": "francois@test.com",
                  "psw": "Password123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("François José"));
    }

    @Test
    void deleteUser_shouldNotReturnBody() throws Exception {
        doNothing().when(useCase).deleteUser(10);

        mockMvc.perform(delete("/api/inscription/users/10"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(useCase).deleteUser(10);
    }

    @Test
    void createUser_shouldAcceptLongNames() throws Exception {
        String longName = "A".repeat(100);
        Users user = new Users(1, longName, "user@test.com", "HASH", false, "PUB", "vault");

        when(useCase.saveUser(any())).thenReturn(user);

        String json = String.format("""
                {
                  "name": "%s",
                  "mail": "user@test.com",
                  "psw": "Password123",
                  "isAdmin": false
                }
                """, longName);

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(longName));
    }

    @Test
    void getAllUsers_shouldPreserveOrderFromUseCase() throws Exception {
        Users user1 = new Users(1, "First", "first@test.com", "HASH1", false, "PUB1", "vault1");
        Users user2 = new Users(2, "Second", "second@test.com", "HASH2", false, "PUB2", "vault2");
        Users user3 = new Users(3, "Third", "third@test.com", "HASH3", true, "PUB3", "vault3");

        when(useCase.getAllUsers()).thenReturn(Arrays.asList(user1, user2, user3));

        mockMvc.perform(get("/api/inscription/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("First"))
                .andExpect(jsonPath("$[1].name").value("Second"))
                .andExpect(jsonPath("$[2].name").value("Third"));
    }

    @Test
    void createUser_shouldReturnContentTypeJson() throws Exception {
        Users user = new Users(1, "Test", "test@test.com", "HASH", false, "PUB", "vault");

        when(useCase.saveUser(any())).thenReturn(user);

        String json = """
                {
                  "name": "Test",
                  "mail": "test@test.com",
                  "psw": "TestPass123",
                  "isAdmin": false
                }
                """;

        mockMvc.perform(post("/api/inscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}