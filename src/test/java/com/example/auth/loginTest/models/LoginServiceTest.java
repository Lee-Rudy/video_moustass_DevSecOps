package com.example.auth.loginTest.models;

import com.example.auth.config.JwtHelper;
import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.login.models.LoginService;
import com.example.auth.login.models.LoginService.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    private SpringDataUsersRepository userRepo;
    private PasswordEncoder encoder;
    private JwtHelper jwtHelper;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        userRepo = mock(SpringDataUsersRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwtHelper = mock(JwtHelper.class);
        loginService = new LoginService(userRepo, encoder, jwtHelper);
    }

    @Test
    void authenticate_returnsTokenAndUserId_whenUserExistsAndPasswordMatches() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        user.setMail("alice@gmail.com");
        user.setPswHash("$2a$10$HASH");

        when(userRepo.findByMail("alice@gmail.com")).thenReturn(Optional.of(user));
        when(encoder.matches("Alice123456789", user.getPswHash())).thenReturn(true);
        when(jwtHelper.createToken(1)).thenReturn("jwt-token-123");

        Optional<LoginResponse> opt = loginService.authenticate("alice@gmail.com", "Alice123456789");

        assertTrue(opt.isPresent());
        assertEquals("jwt-token-123", opt.get().token());
        assertEquals(1, opt.get().userId());
        assertEquals("Alice", opt.get().name());
        verify(userRepo).findByMail("alice@gmail.com");
        verify(encoder).matches("Alice123456789", user.getPswHash());
        verify(jwtHelper).createToken(1);
    }

    @Test
    void authenticate_returnsEmpty_whenMailIsNull() {
        Optional<LoginResponse> opt = loginService.authenticate(null, "password");

        assertTrue(opt.isEmpty());
        verify(userRepo, never()).findByMail(anyString());
    }

    @Test
    void authenticate_returnsEmpty_whenPasswordIsNull() {
        Optional<LoginResponse> opt = loginService.authenticate("alice@gmail.com", null);

        assertTrue(opt.isEmpty());
        verify(userRepo, never()).findByMail(anyString());
    }

    @Test
    void authenticate_returnsEmpty_whenUserNotFound() {
        when(userRepo.findByMail("inconnu@gmail.com")).thenReturn(Optional.empty());

        Optional<LoginResponse> opt = loginService.authenticate("inconnu@gmail.com", "AnyPassword");

        assertTrue(opt.isEmpty());
        verify(encoder, never()).matches(anyString(), anyString());
        verify(jwtHelper, never()).createToken(any());
    }

    @Test
    void authenticate_returnsEmpty_whenPasswordDoesNotMatch() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setMail("alice@gmail.com");
        user.setPswHash("$2a$10$HASH");

        when(userRepo.findByMail("alice@gmail.com")).thenReturn(Optional.of(user));
        when(encoder.matches("WrongPassword", user.getPswHash())).thenReturn(false);

        Optional<LoginResponse> opt = loginService.authenticate("alice@gmail.com", "WrongPassword");

        assertTrue(opt.isEmpty());
        verify(jwtHelper, never()).createToken(any());
    }

    @Test
    void authenticate_normalizesMailToLowerCase() {
        when(userRepo.findByMail("alice@gmail.com")).thenReturn(Optional.empty());

        loginService.authenticate("  ALICE@GMAIL.COM  ", "pwd");

        verify(userRepo).findByMail("alice@gmail.com");
    }
}
