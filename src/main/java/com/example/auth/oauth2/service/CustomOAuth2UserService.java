package com.example.auth.oauth2.service;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.inscription.ports.out.UserKeyVaultPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service personnalisé pour gérer l'authentification OAuth2 des utilisateurs.
 * Crée automatiquement un compte utilisateur s'il n'existe pas encore.
 * 
 * Note: Ce bean n'est créé que si OAuth2 est configuré (client-id défini).
 */
@Service
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.google.client-id")
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SpringDataUsersRepository usersRepository;
    private final UserKeyVaultPort vaultPort;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(
            SpringDataUsersRepository usersRepository,
            UserKeyVaultPort vaultPort,
            PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.vaultPort = vaultPort;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Charge l'utilisateur OAuth2 et crée un compte s'il n'existe pas
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Charge les informations de l'utilisateur depuis le provider OAuth2
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Récupère le provider (google, etc.)
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        // Récupère l'ID unique fourni par le provider
        String providerId = oauth2User.getAttribute("sub");
        
        // Récupère l'email et le nom
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        logger.info("Authentification OAuth2 - Provider: {}, Email: {}, Name: {}", provider, email, name);

        // Recherche ou crée l'utilisateur
        UsersJpaEntity user = findOrCreateUser(provider, providerId, email, name);

        logger.info("Utilisateur OAuth2 chargé avec succès - ID: {}, Email: {}", user.getId(), user.getMail());

        return oauth2User;
    }

    /**
     * Recherche un utilisateur existant ou en crée un nouveau
     */
    private UsersJpaEntity findOrCreateUser(String provider, String providerId, String email, String name) {
        // Recherche par OAuth provider et provider ID
        var userOpt = usersRepository.findByOauthProviderAndOauthProviderId(provider, providerId);

        if (userOpt.isPresent()) {
            logger.info("Utilisateur OAuth2 existant trouvé - ID: {}", userOpt.get().getId());
            return userOpt.get();
        }

        // L'utilisateur n'existe pas, on le crée
        logger.info("Création d'un nouveau compte utilisateur pour OAuth2 - Email: {}", email);
        return createNewOAuth2User(provider, providerId, email, name);
    }

    /**
     * Crée un nouveau utilisateur OAuth2
     */
    private UsersJpaEntity createNewOAuth2User(String provider, String providerId, String email, String name) {
        UsersJpaEntity newUser = new UsersJpaEntity();
        
        // Informations de base
        newUser.setName(name != null ? name : "Utilisateur OAuth2");
        newUser.setMail(email);
        newUser.setAdmin(false);
        
        // Génère un mot de passe aléatoire fort (l'utilisateur n'en aura pas besoin car il utilise OAuth2)
        String randomPassword = generateSecureRandomPassword();
        newUser.setPswHash(passwordEncoder.encode(randomPassword));
        
        // Informations OAuth2
        newUser.setOauthProvider(provider);
        newUser.setOauthProviderId(providerId);
        
        newUser.setCreatedAt(LocalDateTime.now());

        try {
            // Crée une clé Ed25519 dans Vault pour les signatures
            String vaultKeyName = "user-" + System.currentTimeMillis() + "-" + SECURE_RANDOM.nextInt(10000);
            vaultPort.createSigningKey(vaultKeyName);
            
            // Exporte la clé publique
            String publicKey = vaultPort.exportPublicKey(vaultKeyName);
            
            newUser.setVaultKey(vaultKeyName);
            newUser.setPublicKey(publicKey);
            
            logger.info("Clé Vault créée pour l'utilisateur OAuth2 - Vault Key: {}", vaultKeyName);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la clé Vault pour l'utilisateur OAuth2: {}", e.getMessage());
            // On continue sans clé Vault, elle pourra être créée plus tard si nécessaire
            newUser.setVaultKey(null);
            newUser.setPublicKey(null);
        }

        // Sauvegarde l'utilisateur
        UsersJpaEntity savedUser = usersRepository.save(newUser);
        logger.info("Nouveau compte OAuth2 créé avec succès - ID: {}, Email: {}", savedUser.getId(), savedUser.getMail());

        return savedUser;
    }

    /**
     * Génère un mot de passe aléatoire sécurisé de 32 caractères
     */
    private String generateSecureRandomPassword() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
