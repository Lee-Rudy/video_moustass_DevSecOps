package com.example.auth.oauth2Test.service;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import com.example.auth.oauth2.service.CustomOAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CustomOAuth2UserService
 */
class CustomOAuth2UserServiceTest {

    private SpringDataUsersRepository usersRepository;
    private UserKeyVaultPort vaultPort;
    private PasswordEncoder passwordEncoder;
    private CustomOAuth2UserService oauth2UserService;

    @BeforeEach
    void setUp() {
        usersRepository = mock(SpringDataUsersRepository.class);
        vaultPort = mock(UserKeyVaultPort.class);
        passwordEncoder = mock(PasswordEncoder.class);
        oauth2UserService = new CustomOAuth2UserService(usersRepository, vaultPort, passwordEncoder);
    }

    @Test
    void loadUser_returnsExistingUser_whenUserAlreadyExists() {
        // Arrange
        String providerId = "google-user-123";
        String email = "user@example.com";
        String name = "John Doe";

        UsersJpaEntity existingUser = new UsersJpaEntity();
        existingUser.setId(1);
        existingUser.setMail(email);
        existingUser.setName(name);
        existingUser.setOauthProvider("google");
        existingUser.setOauthProviderId(providerId);

        // Mock OAuth2User retourné par super.loadUser()
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", providerId);
        attributes.put("email", email);
        attributes.put("name", name);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "sub"
        );

        when(usersRepository.findByOauthProviderAndOauthProviderId("google", providerId))
            .thenReturn(Optional.of(existingUser));

        OAuth2UserRequest userRequest = createOAuth2UserRequest();
        
        // Spy pour mocker super.loadUser()
        CustomOAuth2UserService spyService = Mockito.spy(oauth2UserService);
        doReturn(mockOAuth2User).when(spyService).loadUser(userRequest);

        // Act
        OAuth2User result = spyService.loadUser(userRequest);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        assertEquals(name, result.getAttribute("name"));
        
        verify(usersRepository, times(1)).findByOauthProviderAndOauthProviderId("google", providerId);
        verify(usersRepository, never()).save(any());
    }

    @Test
    void loadUser_createsNewUser_whenUserDoesNotExist() {
        // Arrange
        String providerId = "google-user-456";
        String email = "newuser@example.com";
        String name = "Jane Doe";

        UsersJpaEntity savedUser = new UsersJpaEntity();
        savedUser.setId(2);
        savedUser.setMail(email);
        savedUser.setName(name);
        savedUser.setOauthProvider("google");
        savedUser.setOauthProviderId(providerId);

        // Mock OAuth2User retourné par super.loadUser()
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", providerId);
        attributes.put("email", email);
        attributes.put("name", name);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "sub"
        );

        when(usersRepository.findByOauthProviderAndOauthProviderId("google", providerId))
            .thenReturn(Optional.empty());
        when(usersRepository.save(any(UsersJpaEntity.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted-password");
        doNothing().when(vaultPort).createSigningKey(anyString());
        when(vaultPort.exportPublicKey(anyString())).thenReturn("public-key-data");

        OAuth2UserRequest userRequest = createOAuth2UserRequest();
        
        // Spy pour mocker super.loadUser()
        CustomOAuth2UserService spyService = Mockito.spy(oauth2UserService);
        doReturn(mockOAuth2User).when(spyService).loadUser(userRequest);

        // Act
        OAuth2User result = spyService.loadUser(userRequest);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        assertEquals(name, result.getAttribute("name"));

        verify(usersRepository, times(1)).findByOauthProviderAndOauthProviderId("google", providerId);
        verify(usersRepository, times(1)).save(any(UsersJpaEntity.class));
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(vaultPort, times(1)).createSigningKey(anyString());
        verify(vaultPort, times(1)).exportPublicKey(anyString());
    }

    @Test
    void loadUser_createsUserWithoutVaultKey_whenVaultOperationFails() {
        // Arrange
        String providerId = "google-user-789";
        String email = "user3@example.com";
        String name = "Bob Smith";

        UsersJpaEntity savedUser = new UsersJpaEntity();
        savedUser.setId(3);
        savedUser.setMail(email);
        savedUser.setName(name);
        savedUser.setOauthProvider("google");
        savedUser.setOauthProviderId(providerId);

        // Mock OAuth2User retourné par super.loadUser()
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", providerId);
        attributes.put("email", email);
        attributes.put("name", name);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "sub"
        );

        when(usersRepository.findByOauthProviderAndOauthProviderId("google", providerId))
            .thenReturn(Optional.empty());
        when(usersRepository.save(any(UsersJpaEntity.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted-password");
        doThrow(new RuntimeException("Vault error")).when(vaultPort).createSigningKey(anyString());

        OAuth2UserRequest userRequest = createOAuth2UserRequest();
        
        // Spy pour mocker super.loadUser()
        CustomOAuth2UserService spyService = Mockito.spy(oauth2UserService);
        doReturn(mockOAuth2User).when(spyService).loadUser(userRequest);

        // Act
        OAuth2User result = spyService.loadUser(userRequest);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        assertEquals(name, result.getAttribute("name"));

        verify(usersRepository, times(1)).save(any(UsersJpaEntity.class));
        verify(vaultPort, times(1)).createSigningKey(anyString());
        verify(vaultPort, never()).exportPublicKey(anyString());
    }

    @Test
    void loadUser_setsDefaultName_whenNameIsNull() {
        // Arrange
        String providerId = "google-user-999";
        String email = "noname@example.com";

        UsersJpaEntity savedUser = new UsersJpaEntity();
        savedUser.setId(4);
        savedUser.setMail(email);
        savedUser.setName("Utilisateur OAuth2");
        savedUser.setOauthProvider("google");
        savedUser.setOauthProviderId(providerId);

        // Mock OAuth2User retourné par super.loadUser() avec name null
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", providerId);
        attributes.put("email", email);
        attributes.put("name", null); // Name est null
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "sub"
        );

        when(usersRepository.findByOauthProviderAndOauthProviderId("google", providerId))
            .thenReturn(Optional.empty());
        when(usersRepository.save(any(UsersJpaEntity.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted-password");
        doNothing().when(vaultPort).createSigningKey(anyString());
        when(vaultPort.exportPublicKey(anyString())).thenReturn("public-key-data");

        OAuth2UserRequest userRequest = createOAuth2UserRequest();
        
        // Spy pour mocker super.loadUser()
        CustomOAuth2UserService spyService = Mockito.spy(oauth2UserService);
        doReturn(mockOAuth2User).when(spyService).loadUser(userRequest);

        // Act
        OAuth2User result = spyService.loadUser(userRequest);

        // Assert
        assertNotNull(result);
        verify(usersRepository, times(1)).save(argThat(user -> 
            "Utilisateur OAuth2".equals(user.getName())
        ));
    }

    @Test
    void loadUser_setsIsAdminToFalse_forNewOAuth2Users() {
        // Arrange
        String providerId = "google-user-111";
        String email = "newadmin@example.com";
        String name = "Admin User";

        UsersJpaEntity savedUser = new UsersJpaEntity();
        savedUser.setId(5);
        savedUser.setAdmin(false);

        // Mock OAuth2User retourné par super.loadUser()
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", providerId);
        attributes.put("email", email);
        attributes.put("name", name);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "sub"
        );

        when(usersRepository.findByOauthProviderAndOauthProviderId("google", providerId))
            .thenReturn(Optional.empty());
        when(usersRepository.save(any(UsersJpaEntity.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted-password");
        doNothing().when(vaultPort).createSigningKey(anyString());
        when(vaultPort.exportPublicKey(anyString())).thenReturn("public-key-data");

        OAuth2UserRequest userRequest = createOAuth2UserRequest();
        
        // Spy pour mocker super.loadUser()
        CustomOAuth2UserService spyService = Mockito.spy(oauth2UserService);
        doReturn(mockOAuth2User).when(spyService).loadUser(userRequest);

        // Act
        spyService.loadUser(userRequest);

        // Assert
        verify(usersRepository, times(1)).save(argThat(user -> !user.isAdmin()));
    }

    /**
     * Helper method pour créer un OAuth2UserRequest de test
     */
    private OAuth2UserRequest createOAuth2UserRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost:8082/login/oauth2/code/google")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("sub")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test-access-token",
            null,
            null
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }
}
