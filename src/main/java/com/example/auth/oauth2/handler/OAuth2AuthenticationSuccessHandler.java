package com.example.auth.oauth2.handler;

import com.example.auth.inscription.adapters.out.UsersJpaEntity;
import com.example.auth.inscription.ports.out.SpringDataUsersRepository;
import com.example.auth.mfa.service.MfaService;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handler de succès d'authentification OAuth2.
 * Génère un code MFA et redirige vers le frontend pour la vérification MFA.
 * 
 * Note: Ce bean n'est créé que si OAuth2 est configuré (client-id défini).
 */
@Component
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.google.client-id")
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final MfaService mfaService;
    private final SpringDataUsersRepository usersRepository;

    @Value("${app.frontend.origin}")
    private String frontendOrigin;

    public OAuth2AuthenticationSuccessHandler(
            MfaService mfaService,
            SpringDataUsersRepository usersRepository) {
        this.mfaService = mfaService;
        this.usersRepository = usersRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        logger.info("Authentification OAuth2 réussie");

        try {
            // Récupère l'utilisateur OAuth2
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String providerId = oauth2User.getAttribute("sub");

            logger.info("Utilisateur OAuth2 authentifié - Email: {}, Name: {}", email, name);

            // Recherche l'utilisateur en base de données
            // Note: L'utilisateur devrait déjà exister car CustomOAuth2UserService l'a créé
            UsersJpaEntity user = findUserByOAuth2Info(email, providerId);

            if (user == null) {
                logger.error("Utilisateur OAuth2 non trouvé en base de données - Email: {}", email);
                redirectToErrorPage(response, "Erreur lors de la récupération des informations utilisateur");
                return;
            }

            // Génère et envoie le code MFA
            String sessionToken;
            try {
                sessionToken = mfaService.createAndSendMfaCode(
                    user.getId(),
                    user.getMail(),
                    user.getName()
                );
                logger.info("Code MFA généré et envoyé pour l'utilisateur ID: {}", user.getId());
            } catch (MessagingException e) {
                logger.error("Échec de l'envoi du code MFA: {}", e.getMessage());
                redirectToErrorPage(response, "Échec de l'envoi du code de vérification");
                return;
            }

            // Redirige vers le frontend avec le session token
            String redirectUrl = String.format(
                "%s/?mfa=true&session=%s&email=%s&name=%s",
                frontendOrigin,
                URLEncoder.encode(sessionToken, StandardCharsets.UTF_8),
                URLEncoder.encode(user.getMail(), StandardCharsets.UTF_8),
                URLEncoder.encode(user.getName(), StandardCharsets.UTF_8)
            );

            logger.info("Redirection vers le frontend pour MFA - URL: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'authentification OAuth2: {}", e.getMessage(), e);
            redirectToErrorPage(response, "Erreur inattendue lors de l'authentification");
        }
    }

    /**
     * Recherche l'utilisateur en base de données par email ou OAuth provider ID
     */
    private UsersJpaEntity findUserByOAuth2Info(String email, String providerId) {
        // Essaie de trouver par OAuth provider ID d'abord
        var userOpt = usersRepository.findByOauthProviderAndOauthProviderId("google", providerId);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // Sinon essaie par email
        return usersRepository.findByMail(email).orElse(null);
    }

    /**
     * Redirige vers une page d'erreur sur le frontend
     */
    private void redirectToErrorPage(HttpServletResponse response, String errorMessage) throws IOException {
        String redirectUrl = String.format(
            "%s/?error=%s",
            frontendOrigin,
            URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)
        );
        response.sendRedirect(redirectUrl);
    }
}
