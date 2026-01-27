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
}