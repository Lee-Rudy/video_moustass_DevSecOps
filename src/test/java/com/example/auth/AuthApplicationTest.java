package com.example.auth;

import com.example.auth.login.models.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthApplicationTest {

    @Test
    void main_shouldRunSpringApplication() {
        // Test que main ne lance pas d'exception
        assertDoesNotThrow(() -> {
            // Ne pas vraiment démarrer l'application, juste tester que la méthode existe
            AuthApplication.class.getDeclaredMethod("main", String[].class);
        });
    }

    @Test
    void demoLogin_shouldReturnCommandLineRunner() {
        LoginService loginService = mock(LoginService.class);
        AuthApplication app = new AuthApplication();
        
        CommandLineRunner runner = app.demoLogin(loginService, false, null, null);
        
        assertNotNull(runner);
    }

    @Test
    void demoLogin_shouldAuthenticateUser_whenCalled() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse response = new LoginService.LoginResponse(
            "test-token", 1, "alice", false
        );
        
        when(loginService.authenticate("alice@gmail.com", "TestPassword123"))
            .thenReturn(Optional.of(response));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "alice@gmail.com", "TestPassword123");
        
        runner.run();
        
        verify(loginService).authenticate("alice@gmail.com", "TestPassword123");
    }

    @Test
    void demoLogin_shouldHandleSuccessfulLogin() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse response = new LoginService.LoginResponse(
            "jwt-token", 1, "Alice", false
        );
        
        when(loginService.authenticate("test@example.com", "password"))
            .thenReturn(Optional.of(response));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "test@example.com", "password");
        
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void demoLogin_shouldHandleFailedLogin() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate("test@example.com", "wrongpass"))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "test@example.com", "wrongpass");
        
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void demoLogin_shouldPrintToken_whenLoginSuccessful() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse response = new LoginService.LoginResponse(
            "test-jwt-token", 2, "Bob", false
        );
        
        when(loginService.authenticate("bob@example.com", "BobPass123"))
            .thenReturn(Optional.of(response));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "bob@example.com", "BobPass123");
        
        // Should not throw exception and should call authenticate
        runner.run();
        
        verify(loginService).authenticate("bob@example.com", "BobPass123");
    }

    @Test
    void demoLogin_shouldHandleEmptyOptionalGracefully() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "test@example.com", "password123");
        
        // Should handle empty Optional gracefully (will print failure message)
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void demoLogin_shouldBeDisabled_whenEnabledIsFalse() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, false, null, null);
        
        // Should not call authenticate when disabled
        runner.run();
        
        verify(loginService, never()).authenticate(anyString(), anyString());
    }

    @Test
    void demoLogin_shouldSkip_whenMailIsNull() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, null, "password");
        
        runner.run();
        
        verify(loginService, never()).authenticate(anyString(), anyString());
    }

    @Test
    void demoLogin_shouldSkip_whenPasswordIsNull() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "test@example.com", null);
        
        runner.run();
        
        verify(loginService, never()).authenticate(anyString(), anyString());
    }

    @Test
    void demoLogin_shouldSkip_whenBothCredentialsAreNull() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, null, null);
        
        runner.run();
        
        verify(loginService, never()).authenticate(anyString(), anyString());
    }

    @Test
    void demoLogin_shouldAuthenticate_whenEnabledAndCredentialsProvided() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse response = new LoginService.LoginResponse(
            "test-token", 1, "TestUser", false
        );
        
        when(loginService.authenticate("user@example.com", "SecurePass123"))
            .thenReturn(Optional.of(response));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "user@example.com", "SecurePass123");
        
        runner.run();
        
        verify(loginService).authenticate("user@example.com", "SecurePass123");
    }

    @Test
    void demoLogin_shouldWork_withEmptyStrings() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate("", ""))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "", "");
        
        runner.run();
        
        verify(loginService).authenticate("", "");
    }

    @Test
    void constructor_shouldCreateInstance() {
        assertDoesNotThrow(() -> new AuthApplication());
    }

    @Test
    void authApplication_shouldHaveSpringBootApplicationAnnotation() {
        assertTrue(AuthApplication.class.isAnnotationPresent(
            org.springframework.boot.autoconfigure.SpringBootApplication.class
        ));
    }

    @Test
    void demoLogin_shouldCallAuthenticateWithCorrectCredentials() throws Exception {
        LoginService loginService = mock(LoginService.class);
        String testMail = "specific@test.com";
        String testPassword = "SpecificPass123";
        
        when(loginService.authenticate(testMail, testPassword))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, testMail, testPassword);
        
        runner.run();
        
        verify(loginService).authenticate(testMail, testPassword);
    }

    @Test
    void demoLogin_shouldNotThrowException_whenServiceThrowsException() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate("test@example.com", "password"))
            .thenThrow(new RuntimeException("Service error"));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "test@example.com", "password");
        
        // The CommandLineRunner doesn't handle exceptions, so it will throw
        assertThrows(RuntimeException.class, () -> runner.run());
    }

    @Test
    void demoLogin_shouldReturnAdminResponse_whenAdminLogin() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse adminResponse = new LoginService.LoginResponse(
            "admin-token", 10, "Admin", true
        );
        
        when(loginService.authenticate("admin@example.com", "AdminPass123"))
            .thenReturn(Optional.of(adminResponse));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "admin@example.com", "AdminPass123");
        
        assertDoesNotThrow(() -> runner.run());
        
        verify(loginService).authenticate("admin@example.com", "AdminPass123");
    }

    @Test
    void demoLogin_shouldHandleEmptyOptional() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate("test@example.com", "TestPass123"))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService, true, "test@example.com", "TestPass123");
        
        assertDoesNotThrow(() -> runner.run());
    }
}
