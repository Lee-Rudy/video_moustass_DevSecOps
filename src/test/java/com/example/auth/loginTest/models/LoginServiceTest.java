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
    void authenticate_returnsTokenAndUserIdAndIsAdmin_whenUserExistsAndPasswordMatches() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(1);
        user.setName("Alice");
        user.setMail("alice@gmail.com");
        user.setPswHash("$2a$10$HASH");
        user.setAdmin(false);

        when(userRepo.findAllByMail("alice@gmail.com")).thenReturn(java.util.Collections.singletonList(user));
        when(encoder.matches("Alice123456789", user.getPswHash())).thenReturn(true);
        when(jwtHelper.createToken(1)).thenReturn("jwt-token-123");

        Optional<LoginResponse> opt = loginService.authenticate("alice@gmail.com", "Alice123456789");

        assertTrue(opt.isPresent());
        assertEquals("jwt-token-123", opt.get().token());
        assertEquals(1, opt.get().userId());
        assertEquals("Alice", opt.get().name());
        assertFalse(opt.get().isAdmin());
        verify(userRepo).findAllByMail("alice@gmail.com");
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
        when(userRepo.findAllByMail("inconnu@gmail.com")).thenReturn(java.util.Collections.emptyList());

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

        when(userRepo.findAllByMail("alice@gmail.com")).thenReturn(java.util.Collections.singletonList(user));
        when(encoder.matches("WrongPassword", user.getPswHash())).thenReturn(false);

        Optional<LoginResponse> opt = loginService.authenticate("alice@gmail.com", "WrongPassword");

        assertTrue(opt.isEmpty());
        verify(jwtHelper, never()).createToken(any());
    }

    @Test
    void authenticate_normalizesMailToLowerCase() {
        when(userRepo.findAllByMail("alice@gmail.com")).thenReturn(java.util.Collections.emptyList());

        loginService.authenticate("  ALICE@GMAIL.COM  ", "pwd");

        verify(userRepo).findAllByMail("alice@gmail.com");
    }

    @Test
    void authenticate_returnsIsAdminTrue_whenUserIsAdmin() {
        UsersJpaEntity adminUser = new UsersJpaEntity();
        adminUser.setId(10);
        adminUser.setName("Admin");
        adminUser.setMail("admin@gmail.com");
        adminUser.setPswHash("$2a$10$ADMINHASH");
        adminUser.setAdmin(true);

        when(userRepo.findAllByMail("admin@gmail.com")).thenReturn(java.util.Collections.singletonList(adminUser));
        when(encoder.matches("AdminPass123", adminUser.getPswHash())).thenReturn(true);
        when(jwtHelper.createToken(10)).thenReturn("admin-token");

        Optional<LoginResponse> opt = loginService.authenticate("admin@gmail.com", "AdminPass123");

        assertTrue(opt.isPresent());
        assertEquals("admin-token", opt.get().token());
        assertEquals(10, opt.get().userId());
        assertEquals("Admin", opt.get().name());
        assertTrue(opt.get().isAdmin());
    }

    @Test
    void authenticate_returnsEmptyStringAsName_whenNameIsNull() {
        UsersJpaEntity user = new UsersJpaEntity();
        user.setId(5);
        user.setName(null); // nom null
        user.setMail("noname@gmail.com");
        user.setPswHash("$2a$10$HASH");
        user.setAdmin(false);

        when(userRepo.findAllByMail("noname@gmail.com")).thenReturn(java.util.Collections.singletonList(user));
        when(encoder.matches("Password123", user.getPswHash())).thenReturn(true);
        when(jwtHelper.createToken(5)).thenReturn("token-5");

        Optional<LoginResponse> opt = loginService.authenticate("noname@gmail.com", "Password123");

        assertTrue(opt.isPresent());
        assertEquals("", opt.get().name());
    }

    @Test
    void authenticate_returnsCorrectUser_whenMultipleUsersWithSameEmail() {
        // Créer deux utilisateurs avec le même email mais des mots de passe différents
        UsersJpaEntity user1 = new UsersJpaEntity();
        user1.setId(1);
        user1.setName("Admin");
        user1.setMail("brunerleerudy@gmail.com");
        user1.setPswHash("$2a$10$ADMINHASH");
        user1.setAdmin(true);

        UsersJpaEntity user2 = new UsersJpaEntity();
        user2.setId(5);
        user2.setName("Lee");
        user2.setMail("brunerleerudy@gmail.com");
        user2.setPswHash("$2a$10$LEEHASH");
        user2.setAdmin(false);

        when(userRepo.findAllByMail("brunerleerudy@gmail.com"))
                .thenReturn(java.util.Arrays.asList(user1, user2));
        
        // Test avec le mot de passe de Lee
        when(encoder.matches("Admin123456789", user1.getPswHash())).thenReturn(false);
        when(encoder.matches("Admin123456789", user2.getPswHash())).thenReturn(false);
        when(encoder.matches("Lee123456789", user1.getPswHash())).thenReturn(false);
        when(encoder.matches("Lee123456789", user2.getPswHash())).thenReturn(true);
        when(jwtHelper.createToken(5)).thenReturn("lee-token");

        Optional<LoginResponse> opt = loginService.authenticate("brunerleerudy@gmail.com", "Lee123456789");

        assertTrue(opt.isPresent());
        assertEquals("lee-token", opt.get().token());
        assertEquals(5, opt.get().userId());
        assertEquals("Lee", opt.get().name());
        assertFalse(opt.get().isAdmin());
    }

    @Test
    void authenticate_returnsFirstMatch_whenMultipleUsersWithSameEmailAndPassword() {
        // Cas improbable mais test de la logique
        UsersJpaEntity user1 = new UsersJpaEntity();
        user1.setId(1);
        user1.setName("First");
        user1.setMail("test@test.com");
        user1.setPswHash("$2a$10$SAMEHASH");
        user1.setAdmin(false);

        UsersJpaEntity user2 = new UsersJpaEntity();
        user2.setId(2);
        user2.setName("Second");
        user2.setMail("test@test.com");
        user2.setPswHash("$2a$10$SAMEHASH");
        user2.setAdmin(false);

        when(userRepo.findAllByMail("test@test.com"))
                .thenReturn(java.util.Arrays.asList(user1, user2));
        when(encoder.matches("SamePassword", "$2a$10$SAMEHASH")).thenReturn(true);
        when(jwtHelper.createToken(1)).thenReturn("first-token");

        Optional<LoginResponse> opt = loginService.authenticate("test@test.com", "SamePassword");

        assertTrue(opt.isPresent());
        assertEquals(1, opt.get().userId());
        assertEquals("First", opt.get().name());
    }
}
