package com.example.auth.oauth2Test.controller;

import com.example.auth.config.JwtHelper;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.mfa.entity.MfaCodeJpaEntity;
import com.example.auth.mfa.service.MfaService;
import com.example.auth.oauth2.controller.OAuth2Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour OAuth2Controller
 */
@WebMvcTest(OAuth2Controller.class)
class OAuth2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MfaService mfaService;

    @MockBean
    private SpringDataUsersRepository usersRepository;

    @MockBean
    private JwtHelper jwtHelper;

    private UsersJpaEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UsersJpaEntity();
        testUser.setId(1);
        testUser.setName("John Doe");
        testUser.setMail("john@example.com");
        testUser.setAdmin(false);
    }

    @Test
    void verifyMfaCode_returnsTokenAndUserInfo_whenCodeIsValid() throws Exception {
        // Arrange
        String sessionToken = "test-session-token";
        String code = "123456";

        when(mfaService.validateMfaCode(sessionToken, code)).thenReturn(testUser.getId());
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtHelper.createToken(testUser.getId())).thenReturn("jwt-token-123");

        Map<String, String> requestBody = Map.of(
            "sessionToken", sessionToken,
            "code", code
        );

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(false));

        verify(mfaService, times(1)).validateMfaCode(sessionToken, code);
        verify(usersRepository, times(1)).findById(testUser.getId());
        verify(jwtHelper, times(1)).createToken(testUser.getId());
    }

    @Test
    void verifyMfaCode_returnsUnauthorized_whenCodeIsInvalid() throws Exception {
        // Arrange
        String sessionToken = "test-session-token";
        String code = "wrong-code";

        when(mfaService.validateMfaCode(sessionToken, code)).thenReturn(null);

        Map<String, String> requestBody = Map.of(
            "sessionToken", sessionToken,
            "code", code
        );

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Code MFA invalide ou expiré"));

        verify(mfaService, times(1)).validateMfaCode(sessionToken, code);
        verify(usersRepository, never()).findById(any());
        verify(jwtHelper, never()).createToken(any());
    }

    @Test
    void verifyMfaCode_returnsBadRequest_whenSessionTokenIsMissing() throws Exception {
        // Arrange
        Map<String, String> requestBody = Map.of("code", "123456");

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Session token requis"));

        verify(mfaService, never()).validateMfaCode(anyString(), anyString());
    }

    @Test
    void verifyMfaCode_returnsBadRequest_whenCodeIsMissing() throws Exception {
        // Arrange
        Map<String, String> requestBody = Map.of("sessionToken", "test-token");

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Code MFA requis"));

        verify(mfaService, never()).validateMfaCode(anyString(), anyString());
    }

    @Test
    void verifyMfaCode_returnsInternalServerError_whenUserNotFound() throws Exception {
        // Arrange
        String sessionToken = "test-session-token";
        String code = "123456";

        when(mfaService.validateMfaCode(sessionToken, code)).thenReturn(testUser.getId());
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        Map<String, String> requestBody = Map.of(
            "sessionToken", sessionToken,
            "code", code
        );

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/verify-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Erreur lors de la récupération des informations utilisateur"));

        verify(jwtHelper, never()).createToken(any());
    }

    @Test
    void resendMfaCode_returnsNewSessionToken_whenValid() throws Exception {
        // Arrange
        String oldSessionToken = "old-session-token";
        String newSessionToken = "new-session-token";

        MfaCodeJpaEntity mfaCode = new MfaCodeJpaEntity();
        mfaCode.setUserId(testUser.getId());
        mfaCode.setSessionToken(oldSessionToken);

        when(mfaService.getMfaCodeBySessionToken(oldSessionToken)).thenReturn(mfaCode);
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(mfaService.createAndSendMfaCode(testUser.getId(), testUser.getMail(), testUser.getName()))
            .thenReturn(newSessionToken);

        Map<String, String> requestBody = Map.of("sessionToken", oldSessionToken);

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/resend-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").value(newSessionToken))
                .andExpect(jsonPath("$.message").value("Nouveau code MFA envoyé avec succès"));

        verify(mfaService, times(1)).getMfaCodeBySessionToken(oldSessionToken);
        verify(mfaService, times(1)).createAndSendMfaCode(testUser.getId(), testUser.getMail(), testUser.getName());
    }

    @Test
    void resendMfaCode_returnsNotFound_whenSessionNotFound() throws Exception {
        // Arrange
        String sessionToken = "invalid-session-token";

        when(mfaService.getMfaCodeBySessionToken(sessionToken)).thenReturn(null);

        Map<String, String> requestBody = Map.of("sessionToken", sessionToken);

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/resend-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Session MFA introuvable"));

        verify(mfaService, times(1)).getMfaCodeBySessionToken(sessionToken);
        verify(mfaService, never()).createAndSendMfaCode(any(), anyString(), anyString());
    }

    @Test
    void resendMfaCode_returnsBadRequest_whenSessionTokenIsMissing() throws Exception {
        // Arrange
        Map<String, String> requestBody = Map.of();

        // Act & Assert
        mockMvc.perform(post("/api/oauth2/resend-mfa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Session token requis"));

        verify(mfaService, never()).getMfaCodeBySessionToken(anyString());
    }
}
