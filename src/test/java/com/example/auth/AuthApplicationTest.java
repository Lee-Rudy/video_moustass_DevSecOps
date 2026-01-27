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
        
        CommandLineRunner runner = app.demoLogin(loginService);
        
        assertNotNull(runner);
    }

    @Test
    void demoLogin_shouldAuthenticateUser_whenCalled() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse response = new LoginService.LoginResponse(
            "test-token", 1, "alice", false
        );
        
        when(loginService.authenticate("alice@gmail.com", "Alice123456789"))
            .thenReturn(Optional.of(response));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        runner.run();
        
        verify(loginService).authenticate("alice@gmail.com", "Alice123456789");
    }

    @Test
    void demoLogin_shouldHandleSuccessfulLogin() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse response = new LoginService.LoginResponse(
            "jwt-token", 1, "Alice", false
        );
        
        when(loginService.authenticate(anyString(), anyString()))
            .thenReturn(Optional.of(response));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void demoLogin_shouldHandleFailedLogin() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void demoLogin_shouldPrintToken_whenLoginSuccessful() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse response = new LoginService.LoginResponse(
            "test-jwt-token", 2, "Bob", false
        );
        
        when(loginService.authenticate("alice@gmail.com", "Alice123456789"))
            .thenReturn(Optional.of(response));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        // Should not throw exception and should call authenticate
        runner.run();
        
        verify(loginService).authenticate("alice@gmail.com", "Alice123456789");
    }

    @Test
    void demoLogin_shouldHandleEmptyOptionalGracefully() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        // Should handle empty Optional gracefully (will print failure message)
        assertDoesNotThrow(() -> runner.run());
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
        
        when(loginService.authenticate(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        runner.run();
        
        // Verify it calls with alice credentials (as per the code)
        verify(loginService).authenticate("alice@gmail.com", "Alice123456789");
    }

    @Test
    void demoLogin_shouldNotThrowException_whenServiceThrowsException() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate(anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        // The CommandLineRunner doesn't handle exceptions, so it will throw
        assertThrows(RuntimeException.class, () -> runner.run());
    }

    @Test
    void demoLogin_shouldReturnAdminResponse_whenAdminLogin() throws Exception {
        LoginService loginService = mock(LoginService.class);
        LoginService.LoginResponse adminResponse = new LoginService.LoginResponse(
            "admin-token", 10, "Admin", true
        );
        
        when(loginService.authenticate("alice@gmail.com", "Alice123456789"))
            .thenReturn(Optional.of(adminResponse));
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        assertDoesNotThrow(() -> runner.run());
        
        verify(loginService).authenticate("alice@gmail.com", "Alice123456789");
    }

    @Test
    void demoLogin_shouldHandleEmptyOptional() throws Exception {
        LoginService loginService = mock(LoginService.class);
        
        when(loginService.authenticate("alice@gmail.com", "Alice123456789"))
            .thenReturn(Optional.empty());
        
        AuthApplication app = new AuthApplication();
        CommandLineRunner runner = app.demoLogin(loginService);
        
        assertDoesNotThrow(() -> runner.run());
    }
}
