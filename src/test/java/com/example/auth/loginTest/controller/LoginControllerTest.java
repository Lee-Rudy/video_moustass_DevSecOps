package com.example.auth.loginTest.controller;

import com.example.auth.login.controller.LoginController;
import com.example.auth.login.models.LoginService;
import com.example.auth.login.models.LoginService.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = LoginController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LoginService loginService;

    @Test
    void login_returns200_andTokenUserId_whenCredentialsValid() throws Exception {
        when(loginService.authenticate("alice@gmail.com", "Alice123456789"))
                .thenReturn(Optional.of(new LoginResponse("jwt-token-xyz", 1)));

        String json = """
                { "mail": "alice@gmail.com", "password": "Alice123456789" }
                """;

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(loginService).authenticate("alice@gmail.com", "Alice123456789");
    }

    @Test
    void login_returns401_whenCredentialsInvalid() throws Exception {
        when(loginService.authenticate("alice@gmail.com", "WrongPassword"))
                .thenReturn(Optional.empty());

        String json = """
                { "mail": "alice@gmail.com", "password": "WrongPassword" }
                """;

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());

        verify(loginService).authenticate("alice@gmail.com", "WrongPassword");
    }
}
