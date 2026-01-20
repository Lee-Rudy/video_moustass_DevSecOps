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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.mail").value("alice@gmail.com"))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.vaultKey").value("vault-key-1"));
    }
}
