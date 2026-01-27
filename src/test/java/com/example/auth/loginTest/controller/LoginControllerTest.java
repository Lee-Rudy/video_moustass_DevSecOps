package com.example.auth.loginTest.controller;

import com.example.auth.audit.service.AuditLogService;
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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(value = LoginController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LoginService loginService;

    @MockBean
    AuditLogService auditLogService;

    @Test
    void login_returns200_andTokenUserIdNameIsAdmin_whenCredentialsValid() throws Exception {
        when(loginService.authenticate("alice@gmail.com", "Alice123456789"))
                .thenReturn(Optional.of(new LoginResponse("jwt-token-xyz", 1, "Alice", false)));

        String json = """
                { "mail": "alice@gmail.com", "password": "Alice123456789" }
                """;

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.userId").value(anyOf(equalTo(1), equalTo(1L))))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.isAdmin").value(false));

        verify(loginService).authenticate("alice@gmail.com", "Alice123456789");
        verify(auditLogService).logAction(anyInt(), anyString(), anyString(), anyInt(), anyString(), any());
    }

    @Test
    void login_returns200_withAdminTrue_whenUserIsAdmin() throws Exception {
        when(loginService.authenticate("admin@gmail.com", "Admin123456789"))
                .thenReturn(Optional.of(new LoginResponse("jwt-token-admin", 10, "Admin", true)));

        String json = """
                { "mail": "admin@gmail.com", "password": "Admin123456789" }
                """;

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-admin"))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.name").value("Admin"))
                .andExpect(jsonPath("$.isAdmin").value(true));

        verify(loginService).authenticate("admin@gmail.com", "Admin123456789");
        verify(auditLogService).logAction(eq(10), eq("USER_LOGIN"), eq("users"), eq(10), anyString(), any());
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
        verify(auditLogService, never()).logAction(anyInt(), anyString(), anyString(), anyInt(), anyString(), any());
    }

    @Test
    void login_logsAuditEvent_whenLoginSuccessful() throws Exception {
        when(loginService.authenticate("alice@gmail.com", "Alice123456789"))
                .thenReturn(Optional.of(new LoginResponse("jwt-token-xyz", 1, "Alice", false)));

        String json = """
                { "mail": "alice@gmail.com", "password": "Alice123456789" }
                """;

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(auditLogService).logAction(
                eq(1),
                eq("USER_LOGIN"),
                eq("users"),
                eq(1),
                argThat(msg -> msg.contains("Connexion réussie")),
                any()
        );
    }
}
